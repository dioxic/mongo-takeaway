import { connect } from 'react-redux'
import { filter } from '../redux/order'
import Link from '../components/Link'

const mapStateToProps = (state, ownProps) => ({
  active: ownProps.filter === state[ownProps.storePath].filter
})

const mapDispatchToProps = (dispatch, ownProps) => ({
  onClick: () => dispatch(filter(ownProps.filter))
})

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Link)