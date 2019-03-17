import { getOrder, postOrder } from './api';
import { createFetch, FILTER } from './fetch';

const STORE_PATH = 'orders';

export const VisibilityFilters = {
	SHOW_ALL: 'SHOW_ALL',
	SHOW_COMPLETED: 'SHOW_COMPLETED',
	SHOW_ACTIVE: 'SHOW_ACTIVE'
}

// Action creators
export function load(id) {
  return createFetch(STORE_PATH + ".ONE", getOrder, id);
}
export function loadAll(id) {
  return createFetch(STORE_PATH + ".MANY", getOrder, id);
}
export function saveOrder(order) {
  return createFetch(STORE_PATH + ".POST", postOrder, order);
}

export const filter = filter => ({
  type: FILTER,
  storePath: STORE_PATH,
  filter
})

// Selectors
export function selectOrders(state) {
  return state[STORE_PATH].data;
}
export function selectFetching(state) {
  return state[STORE_PATH].fetching;
}
export function selectError(state) {
  return state[STORE_PATH].error;
}
export function selectFilter(state) {
  return state[STORE_PATH].filter;
}