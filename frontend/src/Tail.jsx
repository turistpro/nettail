import React from 'react';
import { connect } from 'react-redux';
import * as qs from 'query-string';
import { addLog, addFilter, changeSearchText } from './action';
import { List, AutoSizer, CellMeasurer, CellMeasurerCache } from 'react-virtualized';

import Log from './Log';

const cache = new CellMeasurerCache({
    defaultHeight: 15,
    minHeight: 10,
    fixedWidth: true
});

class TailVirtual extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            headerHeight: 0
          }
        this.handleInputChange = this.handleInputChange.bind(this);
        this.renderRow = this.renderRow.bind(this);
        this.updateHeaderHeigth = this.updateHeaderHeigth.bind(this);
    }
    componentDidMount() {
        const host = window.location.host;
        // const host = "localhost:8080";
        const tailWs = new WebSocket(`ws://${host}/api/tail/${this.props.path}`);
        tailWs.onmessage = message => {
            this.props.addLog(message.data);
        };
        tailWs.onerror = error => {
            console.log(error);
            this.props.addLog(error.data);
        }
        document.title = "netTail -f " + this.props.path;
        this.updateHeaderHeigth();
        this.searchElement.value = this.props.searchText;
    }

    updateHeaderHeigth() {
        const headerHeight = this.headerElement === undefined ? 0 : this.headerElement.clientHeight;
        this.setState({ headerHeight });
    }


    handleInputChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.name;
        this.props.changeSearchText(value);
    }
    renderRow({
        key,         // Unique key within array of rows
        index,       // Index of row within collection
        parent,
        isScrolling, // The List is currently being scrolled
        isVisible,   // This row is visible within the List (eg it is not an overscanned row)
        style        // Style object to be applied to row (to position it)
    }) {
        return (
            <CellMeasurer
                cache={cache}
                columnIndex={0}
                key={key}
                parent={parent}
                rowIndex={index}
            >
                <div key={key} style={style} >
                    <Log id={this.props.filtered[index]} message={this.props.logs[this.props.filtered[index]]} />
                </div>
            </CellMeasurer>
        )
    }
    render() {
        const overall = this.props.count;
        const logs = this.props.filtered;
        const count = logs.length;
        const searchText = this.props.searchText;
        document.title = `(${overall}) netTail -f ${this.props.path}`;
        const badge = (searchText !== undefined && searchText.length > 0) ? `(${count}:${overall})` : `(${overall})`;
        return (

            <AutoSizer
                onResize={() => {
                    cache.clearAll();
                    this.updateHeaderHeigth();
                }}
            >
                {({ height, width }) => (
                    <div className="container">
                        <div className="navbar" ref={ (headerElement) => this.headerElement = headerElement }>
                            <div className="item" style={ {maxWidth: width-270 + 'px'} }>
                                <h3>netTail -f {this.props.path} {badge}</h3>
                            </div>
                            <div className="item rigth">
                                <input id="search" type="text" name="filter" ref={ (e) => this.searchElement = e } placeholder="search" onChange={this.handleInputChange} />
                            </div>
                        </div>
                        <div className="main" style={{top: this.state.headerHeight + 'px'}}>
                            <List
                                height={height-this.state.headerHeight}
                                rowCount={count}
                                deferredMeasurementCache={cache}
                                rowHeight={cache.rowHeight}
                                rowRenderer={row => this.renderRow(row)}
                                width={width}
                                scrollToIndex={count}
                            />
                        </div>
                    </div>

                )}
            </AutoSizer>

        );
    }
}

const mapStateToProps = state => {
    const query = qs.parse(state.routing.location.search);
    const searchText = query.searchText || '';
    
    let reg = undefined;
    try {
        reg = RegExp(searchText);
    } catch (e) {}

    const logs = state.app.logs || [];
    const filtered = (state.app.logKeys || []).filter(item => {
        const line = logs[item];
        return searchText === '' || (reg !== undefined && line.search(reg) >= 0) || line.includes(searchText);
    });

    return {
        path: query.path,
        searchText: searchText,
        filtered: filtered,
        logs: logs,
        count: state.app.count,
    }
};

const mapDispatchToProps = {
    addLog,
    changeSearchText
}


export default connect(mapStateToProps, mapDispatchToProps)(TailVirtual);