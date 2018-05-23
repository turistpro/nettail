import React from 'react';
import { connect } from 'react-redux';
import * as qs from 'query-string';
import { addLog } from './action';



class Tail extends React.Component {

    componentDidMount() {
        const tailWs = new WebSocket('ws://localhost:8080/api/tail/' + this.props.path);
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
        this.messagesEnd.scrollIntoView({ behavior: "smooth" });
    }

    componentDidUpdate() {
        this.scrollToBottom();
    }

    render() {
        document.title = `(${this.props.logs.length}) netTail -f ${this.props.path}`;
        return (
            <div>
                <h1>netTail -f { this.props.path }</h1>
                <div>
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
    logs: state.app.logs
});

const mapDispatchToProps = {
    addLog
}


export default connect(mapStateToProps, mapDispatchToProps)(Tail);