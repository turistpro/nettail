import React from 'react'
import ReactDOM from 'react-dom';
import { createStore, applyMiddleware, combineReducers} from 'redux';
import { Provider } from 'react-redux';
import { Route, Switch, Redirect } from 'react-router';
import createHistory from 'history/createBrowserHistory';
import { ConnectedRouter, routerReducer, routerMiddleware, push } from 'react-router-redux';
import { composeWithDevTools } from 'redux-devtools-extension';
import thunk from 'redux-thunk';
import { addLog } from './action';

import App from './App';
import Tail from './Tail';

import './style.css';

const rootReducer = (state={ logs: [] , filter: "", count: 0 }, action) => {
  const { type } = action;
  switch(type) {
    case 'ADD_LOG':
      const { logs, count } = state;
      logs.push(action.message);
      return Object.assign({}, state, {count: count + 1});
    case 'ADD_FILTER':
      return Object.assign({}, state, {filter: action.filter}) 
    default:
      return state;
  }
};

const history = createHistory()

const store = createStore(
  combineReducers({
    app: rootReducer,
    routing: routerReducer
  }),
  composeWithDevTools(applyMiddleware(thunk, routerMiddleware(history)))
);


/*const randomStr = (m) => {
  m = m || 9;
  let s = '';
  const r = ' ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789АБВГДЕЖДИВАЙГЙЦХЁафвыауцйуцйисчсй';
	for (let i=0; i < m; i++) {
    s += r.charAt(Math.floor(Math.random()*r.length));
  }
	return s;
};

for (var i = 0; i < 100; i++) {
  store.dispatch(addLog('message-' + i));
}

setInterval(() => store.dispatch(
  addLog(
    new Date() + " - " + randomStr(100) + randomStr(100)
  )
), 1000);*/

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
