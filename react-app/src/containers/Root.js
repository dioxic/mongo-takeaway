import React, { Component } from 'react'
import { Provider } from 'react-redux'
import configureStore from '../store/configureStore'
import App from '../components/App'
import { VisibilityFilters } from '../actions'

const store = configureStore({
    orders: {
        data: [],
        filter: VisibilityFilters.SHOW_ALL
    }
})

export default class Root extends Component {
    render() {
        return (
            <Provider store={store}>
                <App />
            </Provider>
        )
    }
}