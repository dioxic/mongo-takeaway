import { connect } from 'react-redux'
import { deliverOrder } from '../actions'
import OrderList from '../components/OrderList'
import { VisibilityFilters } from '../actions'

const getVisibleOrders = ({orders, visibilityFilter}) => {
  switch (visibilityFilter.filter) {
    case VisibilityFilters.SHOW_ALL:
      return orders.items;
    case VisibilityFilters.SHOW_COMPLETED:
      return orders.items.filter(t => t.status === "DELIVERED")
    case VisibilityFilters.SHOW_ACTIVE:
      return orders.items.filter(t => t.status !== "DELIVERED")
    default:
      throw new Error('Unknown filter: ' + visibilityFilter)
  }
}

const mapStateToProps = (state) => ({
  orders: getVisibleOrders(state)
})

const mapDispatchToProps = dispatch => ({
	deliverOrder: order => dispatch(deliverOrder(order))
})

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(OrderList)