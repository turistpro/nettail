import { push } from 'react-router-redux';;
import * as qs from 'query-string';

export const addFilter = searchText => ({
    type: 'ADD_FILTER',
    filter: searchText
})

export const changeSearchText = (searchText) => (dispatch, getState) => {
    const location = getState().routing.location;
    const queryObject = qs.parse(location.search);
    if (searchText===undefined || searchText.toString().length=== 0) {
        delete queryObject.searchText;
    } else {
        queryObject.searchText = searchText;
    }
    const url = location.pathname + '?' + qs.stringify(queryObject);
    dispatch(push(url));
}


export const addLog = message => ({
    type: 'ADD_LOG',
    message
});