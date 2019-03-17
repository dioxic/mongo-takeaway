// Actions
export const REQUEST = 'redux/fetch/REQUEST';
export const SUCCESS = 'redux/fetch/SUCCESS';
export const ERROR = 'redux/fetch/ERROR';
export const FILTER = 'redux/fetch/FILTER';

// Reducer
export default function reducer(state, action = {}) {
  const { domain, storePath, data, error, filter } = action;
  const fetchState = {};

  switch (action.type) {
    case REQUEST:
      fetchState.fetching = true;
      fetchState.error = false;
      break;
    case SUCCESS:
      fetchState.fetching = false;
      fetchState.error = false;
      fetchState.data = data;
      break;
    case ERROR:
      fetchState.fetching = false;
      fetchState.error = error;
      break;
    case FILTER:
      fetchState.filter = filter;
      break;
    default:
      return state;
  }
  // Replace the current state at `storePath` with the new
  // computed `fetchState`.
  return {
    ...state,
    [domain]: {
      ...state[domain],
      [storePath]: {
        ...state[domain][storePath],
        ...fetchState
      }
    }
  };
}
// Action creators
// Create a fetch action flow.
// * storePath: JSON key for location in the store, e.g. 'user'.
// * api: function that makes a REST call, return a promise
// * apiArgs: list of args to send to `api`
export function createFetch(domain, storePath, api, apiArgs) {
  // Use Thunk middleware to dispatch asynchronously
  return async (dispatch) => {
    dispatch({ type: REQUEST, domain, storePath });

    try {
      const response = await api(apiArgs);
      if (response.ok)
        dispatch({ type: SUCCESS, domain, storePath, response });
      else {
        console.warn(response);
        const error = {
          httpStatus: response['status'],
          msg: response['statusText'],
          url: response['url'],
        }
        dispatch({ type: ERROR, domain, storePath, error })
      }
    } catch (e) {
      // console.error(e);
      const error = {
        msg: e.message || {}
      }
      dispatch({ type: ERROR, domain, storePath, error });
    }
  };
}