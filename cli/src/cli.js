import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

const ipRegex = /(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/

let username
let server

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username>', 'This will connect you to the FTD Chat Server. Requires a username to be signed in.')
  .option('-h, --host <h>', 'IP address to the chat server, defaults localhost')
  .option('-p, --port <p>', 'Port for the chat server, defaults 8080')
  .delimiter(cli.chalk['green'](`<FTD Chat>`))
  .init(function (args, callback) {
    let host = ipRegex.test(args.options.host) ? args.options.host : 'localhost'
    let port = args.options.port === undefined ? 8080 : args.options.port
    username = args.username

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
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /\S+/g) // Used /S+/g to sepearate at the spaces and checks the entire string (global)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'help') {
      this.log('Currently supported commands are:\ndisconnect\nusers\necho (message)\nbroadcast (message)\n@username (message)')
    } else {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    }

    callback()
  })
