import fetch from 'cross-fetch'

/*
 * action types
 */

export const ADD_ORDER = 'ADD_ORDER'
export const UPDATE_ORDER = 'UPDATE_ORDER'
export const DELIVER_ORDER = 'DELIVER_ORDER'
export const RECEIVE_ORDER = 'RECEIVE_ORDER'
export const REQUEST_ORDERS = 'REQUEST_ORDERS'
export const SET_VISIBILITY_FILTER = 'SET_VISIBILITY_FILTER'

/*
 * other constants
 */

export const VisibilityFilters = {
  SHOW_ALL: 'SHOW_ALL',
  SHOW_COMPLETED: 'SHOW_COMPLETED',
  SHOW_ACTIVE: 'SHOW_ACTIVE'
}

/*
 * action creators
 */

export const addOrder = id => ({ 
  type: ADD_ORDER,
  id
})

export const requestOrders = customerId => ({
  type: REQUEST_ORDERS,
  customerId
})

export const setVisibilityFilter = filter => ({ type: SET_VISIBILITY_FILTER, filter })

export const updateOrder = order => ({ type: UPDATE_ORDER, order })

export const deliverOrder = id => ({ type: DELIVER_ORDER, id })

export const receiveOrders = (customerId, json) => ({
  type: RECEIVE_ORDER,
  customerId,
  orders: json.data.children.map(child => child.data),
  receivedAt: Date.now()
})

export const fetchOrders = customerId => dispatch => {
  dispatch(requestOrders(customerId))
  return fetch(`localhost:8080/order?customerId=${customerId}`)
    .then(response => response.json())
    .then(json => dispatch(receiveOrders(customerId, json)))
}