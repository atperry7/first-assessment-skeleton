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
      return `\x1b[31m${this.timestamp}: <${this.username}> ${this.contents}`
    } else if (this.command === 'broadcast') {
      return `\x1b[35m${this.timestamp} <${this.username}> (all): ${this.contents}`
    } else if (this.command === 'users') {
      return `\x1b[32m${this.timestamp}: currently connected users:\n${this.contents}`
    } else if (this.command === 'whisper') {
      return `${this.timestamp} <${this.username}> (whisper): ${this.contents}`
    } else {
      return `\x1b[34m${this.timestamp} <${this.username}> (${this.command}): ${this.contents}`
    }
  }
}
