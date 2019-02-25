import { connect } from 'react-redux'
import { deliverOrder } from '../actions'
import OrderList from '../components/OrderList'
import { VisibilityFilters } from '../actions'

const getVisibleOrders = (orders, filter) => {
  switch (filter) {
    case VisibilityFilters.SHOW_ALL:
      return orders
    case VisibilityFilters.SHOW_COMPLETED:
      return orders.filter(t => t.status === "DELIVERED")
    case VisibilityFilters.SHOW_ACTIVE:
      return orders.filter(t => t.status !== "DELIVERED")
    default:
      throw new Error('Unknown filter: ' + filter)
  }
}

const mapStateToProps = ({orders, visibilityFilter}) => ({
  orders: getVisibleOrders(orders, visibilityFilter)
})

const mapDispatchToProps = dispatch => ({
	deliverOrder: order => dispatch(deliverOrder(order))
})

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(OrderList)