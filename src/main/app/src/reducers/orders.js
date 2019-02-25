import {
	ADD_ORDER,
	UPDATE_ORDER,
	DELIVER_ORDER
} from '../actions'

const orders = (state = [], action) => {
	switch (action.type) {
		case ADD_ORDER:
			return [
				...state,
				{id: action.id, customerId: 0, status: "PENDING"}
			]
		case UPDATE_ORDER:
			return state.map(order =>
				(order === action.id)
					? action.order
					: order

			)
		case DELIVER_ORDER:
			return state.map(order =>
				(order.id === action.id)
					? { ...order, status: "DELIVERED" }
					: order
			)
		default:
			return state
	}
}

export default orders