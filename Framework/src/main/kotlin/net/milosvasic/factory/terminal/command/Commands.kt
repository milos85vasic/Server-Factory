package net.milosvasic.factory.terminal.command

import net.milosvasic.factory.EMPTY
import net.milosvasic.factory.common.filesystem.FilePathBuilder
import net.milosvasic.factory.configuration.variable.Context
import net.milosvasic.factory.configuration.variable.Key
import net.milosvasic.factory.configuration.variable.PathBuilder
import net.milosvasic.factory.configuration.variable.Variable
import net.milosvasic.factory.localhost
import net.milosvasic.factory.remote.Remote
import java.nio.file.InvalidPathException

object Commands {

    const val rm = "rm"
    const val here = "."
    const val cp = "cp -R"
    const val ssh = "ssh -p"
    const val scp = "scp -P"
    const val uname = "uname"
    const val sleep = "sleep"
    const val hostname = "hostname"
    const val tarExtension = ".tar.gz"
    private const val find = "find "

    private const val cd = "cd"
    private const val link = "ln -s"
    private const val netstat = "ss"
    private const val telnet = "telnet"
    private const val mkdir = "mkdir -p"
    private const val chmod = "chmod -R"
    private const val chgrp = "chgrp -R"
    private const val chown = "chown -R"
    private const val openssl = "openssl"
    private const val tarCompress = "tar -cjf"
    private const val tarDecompress = "tar -xvf"

    fun echo(what: String) = "echo '$what'"

    fun printf(what: String) = "printf '$what'"

    fun ssh(user: String = "root", command: String, port: Int = 22, host: String = localhost): String {
        return "$ssh $port $user@$host $command"
    }

    fun ping(host: String, timeoutInSeconds: Int = 3): String {
        return "ping $host -t$timeoutInSeconds"
    }

    fun getHostInfo(): String = "hostnamectl"

    fun getApplicationInfo(application: String): String = "which $application"

    fun reboot(rebootIn: Int = 2) = "( $sleep $rebootIn ; reboot ) & "

    fun grep(what: String) = "grep \"$what\""

    fun scp(what: String, where: String, remote: Remote): String {

        return "$scp ${remote.port} $what ${remote.account}@${remote.host}:$where"
    }

    fun cp(what: String, where: String): String {

        return "$cp $what $where"
    }

    fun find(what: String, where: String) = "$find$where -name \"$what\""

    fun tar(what: String, where: String): String {

        val destination = where.replace(".tar", "").replace(".gz", "")
        return "$tarCompress $destination$tarExtension -C $what ."
    }

    fun unTar(what: String, where: String) = "$tarDecompress $what -C $where"

    fun rm(what: String) = "$rm $what"

    fun chmod(where: String, mode: String) = "$chmod $mode $where"

    fun chgrp(group: String, directory: String) = "$chgrp $group $directory"

    fun chown(account: String, directory: String) = "$chown $account $directory"

    fun mkdir(path: String) = "$mkdir $path"

    fun concatenate(vararg commands: String): String {
        var result = String.EMPTY
        commands.forEach {
            if (result.isNotEmpty() && !result.isBlank()) {
                result += "; "
            }
            result += it
        }
        return result
    }

    fun test(what: String) = "test -e $what"

    fun setHostName(hostname: String) = "hostnamectl set-hostname $hostname"

    fun cat(what: String) = "cat $what"

    fun openssl(command: String) = Variable.parse("$openssl $command")

    @Throws(InvalidPathException::class)
    fun generatePrivateKey(path: String, name: String): String {

        val keyName = getPrivateKyName(name)
        val param = FilePathBuilder()
                .addContext(path)
                .addContext(keyName)
                .build()

        return "$openssl genrsa -out $param"
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun generateRequestKey(path: String, keyName: String, reqName: String): String {

        val params = getOpensslSubject()
        val requestKey = getRequestKeyName(reqName)
        val cmd = "$openssl req -new -key"

        val reqKey = FilePathBuilder()
                .addContext(path)
                .addContext(requestKey)
                .build()

        val verify = "openssl req -in $reqKey -noout -subject"
        val param = FilePathBuilder()
                .addContext(path)
                .addContext(keyName)
                .build()

        return "$cmd $param -out $reqKey -subj $params && $verify"
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun importRequestKey(path: String, requestKey: String, name: String): String {

        val homePath = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.Home)
                .build()

        val home = Variable.get(homePath)
        val key = FilePathBuilder()
                .addContext(path)
                .addContext(requestKey)
                .build()

        return "cd $home && ./easyrsa import-req $key $name"
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun signRequestKey(name: String): String {

        val path = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.Home)
                .build()

        val passPhrasePath = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.Passphrase)
                .build()

        val home = Variable.get(path)
        val passPhrase = Variable.get(passPhrasePath)

        val passIn = "export EASYRSA_PASSIN='pass:$passPhrase'"
        val passOut = "export EASYRSA_PASSOUT='pass:$passPhrase'"
        val passwords = "$passIn && $passOut"
        return "cd $home && $passwords && echo 'yes' | ./easyrsa sign-req server $name"
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun generatePEM(keyName: String = "cakey.pem", certName: String = "cacert.pem"): String {

        val subject = getOpensslSubject()
        val req = "req -subj $subject -new -x509 -extensions v3_ca -keyout $keyName -out $certName -days 3650"

        val path = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.Certificates)
                .build()

        val passPhrasePath = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.Passphrase)
                .build()

        val home = Variable.get(path)
        val passPhrase = Variable.get(passPhrasePath)

        val passIn = "-passin pass:$passPhrase"
        val passOut = "-passout pass:$passPhrase"
        val password = "$passIn $passOut"

        return "cd $home && $openssl $req $password"
    }

    fun getPrivateKyName(name: String): String {
        var fullName = name
        val extension = ".key"
        val prefix = "private."
        if (!fullName.endsWith(extension)) {
            fullName += extension
        }
        if (!fullName.startsWith(prefix)) {
            fullName = prefix + fullName
        }
        return fullName
    }

    fun getRequestKeyName(reqName: String): String {
        var fullName = reqName
        val extension = ".req"
        val prefix = "request."
        if (!fullName.endsWith(extension)) {
            fullName += extension
        }
        if (!fullName.startsWith(prefix)) {
            fullName = prefix + fullName
        }
        return fullName
    }

    fun cd(where: String) = "$cd $where"

    fun link(what: String, where: String) = "$link $what $where"

    fun portAvailable(port: Int) = "! $netstat -tulpn | ${grep(":$port")}"

    fun portTaken(port: Int): String {

        val serverHomePath = PathBuilder()
                .addContext(Context.Server)
                .setKey(Key.ServerHome)
                .build()

        val serverHome = Variable.get(serverHomePath)

        val command = FilePathBuilder()
                .addContext(serverHome)
                .addContext("Utils")
                .addContext("checkPortBound.sh")
                .build()

        return "$command $port"
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun getOpensslSubject(): String {

        val pathHostname = PathBuilder()
                .addContext(Context.Server)
                .setKey(Key.Hostname)
                .build()

        val pathCity = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.City)
                .build()

        val pathCountry = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.Country)
                .build()

        val pathProvince = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.Province)
                .build()

        val pathDepartment = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.Department)
                .build()

        val pathOrganisation = PathBuilder()
                .addContext(Context.Server)
                .addContext(Context.Certification)
                .setKey(Key.Organisation)
                .build()

        val hostname = Variable.get(pathHostname)
        val city = Variable.get(pathCity)
        val country = Variable.get(pathCountry)
        val province = Variable.get(pathProvince)
        val department = Variable.get(pathDepartment)
        val organisation = Variable.get(pathOrganisation)
        var subject = "/C=$country/ST=$province/L=$city/O=$organisation/OU=$department/CN=$hostname"
        subject = subject.replace(" ", "\\ ")
        return subject
    }
}