import { VisibilityFilters, SET_VISIBILITY_FILTER } from '../actions'

const visibilityFilter = (state = {
	isFetching: false,
	didInvalidate: false,
	filter: VisibilityFilters.SHOW_ALL
}, action) => {
	switch (action.type) {
		case SET_VISIBILITY_FILTER:
		return Object.assign({}, state, {
			filter: action.filter
		})
		default:
			return state
	}
}

export default visibilityFilter