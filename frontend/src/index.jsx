import React from 'react'
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers} from 'redux';
import { Provider } from 'react-redux';
import { Route, Switch, Redirect } from 'react-router';
import createHistory from 'history/createBrowserHistory';
import { ConnectedRouter, routerReducer, routerMiddleware, push } from 'react-router-redux';
import { composeWithDevTools } from 'redux-devtools-extension';
import { addLog } from './action';

import App from './App';
import Tail from './Tail';

import './style.css';

const rootReducer = (state={ logs: [] , filter: ""}, action) => {
  const { type } = action;
  switch(type) {
    case 'ADD_LOG':
      return Object.assign({}, state, {
        logs: state.logs.concat(action.message)
      });
    case 'ADD_FILTER':
      return Object.assign({}, state, {filter: action.filter}) 
    default:
      return state;
  }
};

const history = createHistory()
const middleware = routerMiddleware(history)

const store = createStore(
  combineReducers({
    app: rootReducer,
    routing: routerReducer
  }),
  composeWithDevTools(applyMiddleware(middleware))
);

ReactDOM.render(
  <Provider store={store}>
    <ConnectedRouter history={history}>
      <Switch>
        <Route exact={ true } path="/" component={ App } />
        <Route exact={ true } path="/tail" component={ Tail } />
        <Redirect to="/" />
      </Switch>
    </ConnectedRouter>
  </Provider>,
  document.getElementById('app')
);

