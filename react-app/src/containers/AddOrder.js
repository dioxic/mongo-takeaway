import React from 'react'
import { connect } from 'react-redux'
import { saveOrder } from '../actions'

const AddOrder = ({ dispatch }) => {
  let input

  return (
    <div>
      <form onSubmit={e => {
        e.preventDefault()
        if (!input.value.trim()) {
          return
        }
        dispatch(saveOrder(Number(input.value)))
        input.value = ''
      }}>
        <input type="number" ref={node => input = node} />
        <button type="submit">
          Add Order
        </button>
      </form>
    </div>
  )
}

export default connect()(AddOrder)