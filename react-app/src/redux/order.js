import { getOrder } from './api';
import { createFetch, FILTER } from './fetch';

const STORE_PATH = 'orders';


// Action creators
export function load(id) {
  return createFetch(STORE_PATH, getOrder, id);
}
export function loadAll(id) {
  return createFetch(STORE_PATH, getOrder, id);
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