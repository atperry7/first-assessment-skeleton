export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ username, command, contents, timeStamp }) {
    this.username = username
    this.command = command
    this.contents = contents
    this.timestamp = timeStamp
  }

  toJSON () {
    return JSON.stringify({
      username: this.username,
      command: this.command,
      contents: this.contents,
      timestamp: this.timestamp
    })
  }

  toString () {
    if (this.command === 'connect' || this.command === 'disconnect') {
      return `${this.timestamp}: <${this.username}> ${this.contents}`
    } else if (this.command === 'broadcast') {
      return `${this.timestamp} <${this.username}> (all): ${this.contents}`
    } else if (this.command === 'users') {
      return `${this.timestamp}: currently connected users:\n${this.contents}`
    } else {
      return `${this.timestamp} <${this.username}> (${this.command}): ${this.contents}`
    }
  }
}
