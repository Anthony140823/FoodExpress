// Run with node scripts/test-map.cjs. No browser or Android SDK required.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');
const html = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/delivery_map.html'), 'utf8');
const script = html.match(/<script>([\s\S]*?)<\/script>/)[1];

function screen(fetchImpl, leaflet = true) {
  const status = { textContent: '' };
  const handlers = {};
  const markers = [];
  const lines = [];
  const map = {
    setView() { return this; }, fitBounds() { return this; },
    on(event, fn) { handlers[event] = fn; return this; }
  };
  const L = {
    map: () => map,
    divIcon: options => options,
    tileLayer: () => ({ on() { return this; }, addTo() { return this; } }),
    marker(point, options) {
      const marker = { point, options, events: {},
        addTo() { markers.push(this); return this; },
        bindPopup() { return this; },
        setLatLng(p) { this.point = p; return this; },
        getLatLng() { return { lat: this.point[0], lng: this.point[1] }; },
        on(event, fn) { this.events[event] = fn; return this; }
      };
      return marker;
    },
    geoJSON(geometry) {
      lines.push(geometry);
      return { addTo() { return this; }, getBounds() {
        return { extend() { return this; } };
      } };
    }
  };
  const context = vm.createContext({
    window: { L: leaflet ? L : undefined, location: { href: '' } }, L,
    document: { getElementById: () => status },
    fetch: fetchImpl || (() => { throw new Error('Unexpected network call'); }),
    AbortController, setTimeout, clearTimeout
  });
  vm.runInContext(script, context);
  return { context, status, markers, lines, handlers,
    init: config => context.window.initMap(config),
    route: () => context.loadRoute([-12.04, -77.03], [-12.05, -77.04]) };
}

test('initial map does not silently select Lima; tap and drag report actual points', () => {
  const s = screen();
  s.init({ picking: true });
  assert.equal(s.markers.length, 0);
  assert.equal(s.context.window.location.href, '');
  s.handlers.click({ latlng: { lat: -12.04, lng: -77.03 } });
  assert.equal(s.markers.length, 1);
  assert.match(s.context.window.location.href, /^foodexpress:\/\/point\?lat=-12.04&lng=/);
  s.markers[0].point = [-12.06, -77.05];
  s.markers[0].events.dragend();
  assert.match(s.context.window.location.href, /lat=-12.06/);
});

test('existing point can be changed and missing Leaflet explains offline failure', () => {
  const s = screen();
  s.init({ picking: true, point: [-12.04, -77.03] });
  assert.equal(s.markers.length, 1);
  assert.equal(s.markers[0].options.draggable, true);
  const offline = screen(undefined, false);
  offline.init({ picking: true });
  assert.match(offline.status.textContent, /No se pudo cargar/);
});

test('legacy orders never request a route with invented coordinates', () => {
  const s = screen();
  s.init({ picking: false, point: [-12.04, -77.03] });
  assert.equal(s.markers.length, 1);
  assert.match(s.status.textContent, /no tiene la ubicación del restaurante/);
  assert.equal(s.lines.length, 0);
});

test('OSRM longitude/latitude ordering, street geometry and estimates', async () => {
  let requested;
  const s = screen(async url => {
    requested = url;
    return { ok: true, json: async () => ({ code: 'Ok', routes: [{ distance: 2088.4, duration: 298,
      geometry: { type: 'LineString', coordinates: [[-77.03, -12.04], [-77.04, -12.05]] } }] }) };
  });
  s.init({ picking: true });
  await s.route();
  assert.match(requested, /driving\/-77.03,-12.04;-77.04,-12.05/);
  assert.equal(s.lines.length, 1);
  assert.match(s.status.textContent, /2.1 km · 5 min/);
  assert.match(s.status.textContent, /No incluye preparación ni tráfico/);
});

test('network errors and NoRoute never draw a misleading straight line', async () => {
  for (const fetchImpl of [
    async () => { throw new Error('offline'); },
    async () => ({ ok: true, json: async () => ({ code: 'NoRoute', routes: [] }) }),
    async () => ({ ok: false })
  ]) {
    const s = screen(fetchImpl);
    s.init({ picking: true });
    await s.route();
    assert.equal(s.lines.length, 0);
    assert.match(s.status.textContent, /No se pudo calcular/);
  }
});
