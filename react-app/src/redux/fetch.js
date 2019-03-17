// Actions
export const REQUEST = 'redux/fetch/REQUEST';
export const SUCCESS = 'redux/fetch/SUCCESS';
export const ERROR = 'redux/fetch/ERROR';
export const FILTER = 'redux/fetch/FILTER';

// Reducer
export default function reducer(state, action = {}) {
  const { storePath, data, filter } = action;
  const fetchState = Object.assign({},{
    fetching: false,
    saving: false,
    error: false,
  }, state[storePath]);

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
      fetchState.error = true;
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
    [storePath]: {
      ...state[storePath],
      ...fetchState,
    }
  };
}
// Action creators
// Create a fetch action flow.
// * storePath: JSON key for location in the store, e.g. 'user'.
// * api: function that makes a REST call, return a promise
// * apiArgs: list of args to send to `api`
export function createFetch(storePath, api, apiArgs) {
  // Use Thunk middleware to dispatch asynchronously
  return async (dispatch) => {
    dispatch({ type: REQUEST, storePath });

    try {
      const data = await api(...apiArgs);
      dispatch({ type: SUCCESS, storePath, data });
    } catch (e) {
      dispatch({ type: ERROR, storePath });
    }
  };
}