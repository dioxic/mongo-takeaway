import React from 'react';
import { render } from 'react-dom';
import Root from './containers/Root';

// import React from 'react'
// import { render } from 'react-dom'
// import { createStore, applyMiddleware } from 'redux'
// import { Provider } from 'react-redux'
// import thunk from 'redux-thunk'
// import { createLogger } from 'redux-logger'
// import rootReducer from './reducers'
// import App from './components/App'
// import * as serviceWorker from './serviceWorker'
// import './index.css'

// const middleware = [ thunk ]
// if (process.env.NODE_ENV !== 'production') {
//   middleware.push(createLogger())
// }

// const store = createStore(
// 	rootReducer,
// 	applyMiddleware(...middleware))

// render(
//   <Provider store={store}>
//     <App />
//   </Provider>,
//   document.getElementById('root')
// )

render(<Root />, document.getElementById('root'));