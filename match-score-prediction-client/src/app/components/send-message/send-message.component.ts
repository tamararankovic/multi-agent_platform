import { Component, OnInit } from '@angular/core';
import { ACLMessage } from 'src/app/model/acl-message';
import { AID } from 'src/app/model/aid';
import { AgentSocketService } from 'src/app/services/agent-socket.service';
import { AgentService } from 'src/app/services/agent.service';
import { MessageService } from 'src/app/services/message.service';

@Component({
  selector: 'app-send-message',
  templateUrl: './send-message.component.html',
  styleUrls: ['./send-message.component.css']
})
export class SendMessageComponent implements OnInit {

  constructor(private agentService : AgentService, private messageService : MessageService, private agentSocket : AgentSocketService) { 
    this.liveData$.subscribe({
      next : msg => this.handleMessage(msg as string)
    });
  }

  agents : AID[] = []
  performatives : string[] = []

  sender : AID;
  receiver : AID;
  performative : string;
  username : string = '';
  password : string = '';
  senderUsername : string = '';
  receiverUsername : string = '';
  subject : string = '';
  content : string = '';

  liveData$ = this.agentSocket.messages$;

  ngOnInit(): void {
    this.agentSocket.connect();
    this.agentService.getRunningAgents().subscribe(
      data => { this.agents = data;
                this.messageService.getPerformatives().subscribe(
                  data => this.performatives = data
                )
      }
    )
  }

  public send() {
    if(this.agents.includes(this.sender) && this.agents.includes(this.receiver) && this.performatives.includes(this.performative)) {
      const message : ACLMessage = new ACLMessage(this.performative, this.sender, [this.receiver], null, '', null, this.parseUserArgs(), '', '', '', '', '', '', '', 0);
      this.messageService.sendMessage(message).subscribe()
    }
  }

  parseUserArgs() {
    var result : Object = {}
    if(this.performative === 'LOG_IN' || this.performative === 'REGISTER') {
      result['username'] = this.username
      result['password'] = this.password
      return result;
    }
    if(this.performative === 'SEND_MESSAGE_ALL') {
      result['sender'] = this.senderUsername
      result['subject'] = this.subject
      result['content'] = this.content
      return result
     }
     if(this.performative === 'SEND_MESSAGE_USER') {
      result['sender'] = this.senderUsername
      result['receiver'] = this.receiverUsername
      result['subject'] = this.subject
      result['content'] = this.content
      return result
     }
     else {
      result['username'] = this.username
      return result;
     }
  }

  handleMessage(msg : string) {
    this.agents = JSON.parse(msg)
  }
}
