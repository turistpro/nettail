import React from 'react';
import { connect } from 'react-redux';
import * as qs from 'query-string';
import { addLog, addFilter } from './action';



class Tail extends React.Component {

    constructor(props) {
        super(props);
        this.handleInputChange = this.handleInputChange.bind(this);
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
        this.scrollToBottom();
        document.title = "netTail -f " + this.props.path;
    }

    scrollToBottom() {
        this.messagesEnd.scrollIntoView();
    }

    componentDidUpdate() {
        this.scrollToBottom();
    }

    handleInputChange(event) {
        const target = event.target;
        const value = target.type === 'checkbox' ? target.checked : target.value;
        const name = target.name;
        this.props.addFilter(value);
    }

    render() {
        const overall = this.props.count;
        const count = this.props.logs.length;
        const filter = this.props.filter;
        document.title = `(${overall}) netTail -f ${this.props.path}`;
        const badge = (filter.length > 0) ? `(${count}:${overall})` : `(${overall})`;
        return (
            <div>
                <div className="header sticky">
                    <div className="headerInner">
                        <h2>netTail -f { this.props.path } { badge }</h2>
                    </div>
                    <div className="headerInner filter">
                        <input type="text" name="filter" placeholder="search" onChange={this.handleInputChange}/>
                    </div>
                </div>
                <div className="content">
                    <div className="MessageContainer" >
                        <div className="MessagesList">
                            { this.props.logs.map((line, index) => (
                                <div key={ index }>{ line }</div>
                            )) }
                        </div>
                        <div style={{ float:"left", clear: "both" }}
                            ref={(el) => { this.messagesEnd = el; }}>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

const mapStateToProps = state => ({
    path: qs.parse(state.routing.location.search)['path'],
    logs: state.app.logs.filter(line => line.includes(state.app.filter)),
    count: state.app.logs.length,
    filter: state.app.filter
});

const mapDispatchToProps = {
    addLog,
    addFilter
}


export default connect(mapStateToProps, mapDispatchToProps)(Tail);