export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents }) {
    this.username = username
    this.command = command
    this.contents = contents
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }

  toString () {
    if (this.command === 'connect' || this.command === 'disconnect') {
      return `Timestamp: <${this.username}> ${this.contents}`
    } else if (this.command === 'broadcast') {
      return `TimeStamp <${this.username}> (all): ${this.contents}`
    } else {
      return `TimeStamp <${this.username}> (${this.command}): ${this.contents}`
    }
  }
}
