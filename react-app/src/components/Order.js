import React from 'react'
import PropTypes from 'prop-types'

const Order = ({ onClick, completed, id, customerId, status}) => (
  <li
    onClick={onClick}
    style={{
      textDecoration: completed ? 'line-through' : 'none'
    }}
  >
    id: {id} customerId: {customerId} status: {status}
  </li>
)

Order.propTypes = {
  onClick: PropTypes.func.isRequired,
  id: PropTypes.number.isRequired,
  customerId: PropTypes.number.isRequired,
  status: PropTypes.string.isRequired
}

export default Order