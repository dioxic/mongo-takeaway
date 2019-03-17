import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'
import OrderList from '../components/OrderList'
import { VisibilityFilters } from '../redux/order'

import {
  load,
  selectFetching,
  selectError,
  selectOrders,
  selectFilter
} from '../redux/order';

const getVisibleOrders = (data, filter) => {
  console.log("data=" + data + ", filter=" + filter)
  switch (filter) {
    case VisibilityFilters.SHOW_ALL:
      return data;
    case VisibilityFilters.SHOW_COMPLETED:
      return data.filter(t => t.status === "DELIVERED")
    case VisibilityFilters.SHOW_ACTIVE:
      return data.filter(t => t.status !== "DELIVERED")
    default:
      throw new Error('Unknown filter: ' + filter)
  }
}

const mapStateToProps = state => ({
  orders: getVisibleOrders(selectOrders(state), selectFilter(state)),
  loading: selectFetching(state),
  error: selectError(state)
})

const mapDispatchToProps = dispatch =>
  bindActionCreators({ load }, dispatch);

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(OrderList)