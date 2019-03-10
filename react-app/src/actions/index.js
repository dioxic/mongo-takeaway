import fetch from 'cross-fetch'

/*
 * action types
 */

export const SAVE_ORDER = 'SAVE_ORDER'
export const UPDATE_ORDER = 'UPDATE_ORDER'
export const INVALIDATE_ORDER = 'INVALIDATE_ORDER'
export const DELIVER_ORDER = 'DELIVER_ORDER'
export const RECEIVE_ORDERS = 'RECEIVE_ORDERS'
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

export const requestOrders = customerId => ({
	type: REQUEST_ORDERS,
	customerId
})

export const setVisibilityFilter = filter => ({ type: SET_VISIBILITY_FILTER, filter })

export const updateOrder = order => ({ type: UPDATE_ORDER, order })

export const deliverOrder = id => ({ type: DELIVER_ORDER, id })

export const saveOrder = order => ({ type: SAVE_ORDER, order })

export const receiveOrders = json => ({
	type: RECEIVE_ORDERS,
	orders: json.data.children.map(child => child.data),
	receivedAt: Date.now()
})

export const fetchOrders = customerId => dispatch => {
	dispatch(requestOrders(customerId))
	return fetch(`localhost:8080/order?customerId=${customerId}`)
		.then(response => response.json())
		.then(json => dispatch(receiveOrders(json)))
}