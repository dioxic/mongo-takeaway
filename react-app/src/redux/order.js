import { getOrder, postOrder } from './api';
import { createFetch, FILTER } from './fetch';

const STORE_DOMAIN = 'orders';

export const VisibilityFilters = {
  SHOW_ALL: 'SHOW_ALL',
  SHOW_COMPLETED: 'SHOW_COMPLETED',
  SHOW_ACTIVE: 'SHOW_ACTIVE'
}

// Action creators
export function loadOrder(id) {
  return createFetch(STORE_DOMAIN, "ONE", getOrder, id);
}
export function loadOrders() {
  return createFetch(STORE_DOMAIN, "MANY", getOrder);
}
export function saveOrder(order) {
  return createFetch(STORE_DOMAIN, "POST", postOrder, order);
}

export const filter = filter => ({
  type: FILTER,
  storePath: STORE_DOMAIN,
  filter
})

// Selectors
export function selectOrders(state) {
  return state[STORE_DOMAIN].data;
}
export function selectFetching(state) {
  return state[STORE_DOMAIN].fetching;
}
export function selectSaveError(state) {
  return safeSelect(state, "POST").error;
  // return state[STORE_DOMAIN]["POST"].error;
}
export function selectLoadError(state) {
  return safeSelect(state, "MANY").error;
  // return state[STORE_DOMAIN + ".MANY"].error;
}
export function selectFilter(state) {
  return state[STORE_DOMAIN].filter;
}

// Helpers
function safeSelect(state, path) {
  return ((state[STORE_DOMAIN] || {})[path] || {});
}