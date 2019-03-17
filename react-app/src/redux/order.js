import { getOrder, postOrder } from './api';
import { createFetch, FILTER } from './fetch';

const STORE_DOMAIN = 'orders';

export const VisibilityFilters = {
  SHOW_ALL: 'SHOW_ALL',
  SHOW_COMPLETED: 'SHOW_COMPLETED',
  SHOW_ACTIVE: 'SHOW_ACTIVE'
}

const Paths = {
  ONE: 'one',
  MANY: 'many',
  POST: 'post'
}

// Action creators
export function loadOrder(id) {
  return createFetch(STORE_DOMAIN, Paths.ONE, getOrder, id);
}
export function loadOrders() {
  return createFetch(STORE_DOMAIN, Paths.MANY, getOrder);
}
export function saveOrder(order) {
  return createFetch(STORE_DOMAIN, Paths.POST, postOrder, order);
}
export const filter = filter => ({
  type: FILTER,
  domain: STORE_DOMAIN,
  storePath: Paths.MANY,
  filter
})

// Selectors
export function selectOrders(state) {
  return safeSelect(state, Paths.MANY).data;
}
export function selectFetching(state) {
  return safeSelect(state, Paths.MANY).fetching;
}
export function selectSaving(state) {
  return safeSelect(state, Paths.POST).fetching;
}
export function selectSaveError(state) {
  return safeSelect(state, Paths.POST).error;
}
export function selectLoadError(state) {
  return safeSelect(state, Paths.MANY).error;
}
export function selectFilter(state) {
  return safeSelect(state, Paths.MANY).filter;
}

// Helpers
function safeSelect(state, path) {
  return (((state || {})[STORE_DOMAIN] || {})[path] || {});
}