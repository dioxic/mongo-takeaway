import { combineReducers } from 'redux'
import visibilityFilter from './visibilityFilter'
import orders from './orders'

const takeawayApp = combineReducers({
  visibilityFilter,
  orders
})

export default takeawayApp