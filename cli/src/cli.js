import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let commandGlo

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .init(function (args, callback) {
    let host
    let port
    username = args.username
    if (args.host !== undefined) {
      host = args.host
    } else {
      host = 'localhost'
    }

    if (args.post !== undefined) {
      port = Number(args.port)
    } else {
      port = 8080
    }

    server = connect({ host: host, port: port }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      this.log(Message.fromJSON(buffer).toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  }).delimiter(cli.chalk['green'](`<${username}>`))
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input)
    const contents = rest.join(' ')
    const regExp = new RegExp('/[^@a-z]/')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (command === 'broadcast') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else if (regExp.test(command)) {
      this.log('Currently Implementing the whisper method')
    } else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
