import {
	SAVE_ORDER,
	UPDATE_ORDER,
	DELIVER_ORDER,
	REQUEST_ORDERS,
	RECEIVE_ORDERS,
	INVALIDATE_ORDER
} from '../actions'

const orders = (state = {
    isFetching: false,
    didInvalidate: false,
    items: []
  }, action) => {
	switch (action.type) {
		case INVALIDATE_ORDER:
			return Object.assign({}, state, {
				didInvalidate: true
			})
		case SAVE_ORDER:
			return Object.assign({}, state, {
				isFetching: false,
				didInvalidate: false
			})
		case UPDATE_ORDER:
			return state.map(order =>
				(order === action.id)
					? action.order
					: order)
		case RECEIVE_ORDERS:
			return Object.assign({}, state, {
				isFetching: false,
				didInvalidate: false,
				items: action.orders,
				lastUpdated: action.receivedAt
			})
		case DELIVER_ORDER:
			return state.map(order =>
				(order.id === action.id)
					? { ...order, status: "DELIVERED" }
					: order
			)
		case REQUEST_ORDERS:
			return Object.assign({}, state, {
				isFetching: true,
				didInvalidate: false
		  	})
		default:
			return state
	}
}

export default orders