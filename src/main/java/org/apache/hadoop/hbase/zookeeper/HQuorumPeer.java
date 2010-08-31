begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|zookeeper
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|NetworkInterface
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Enumeration
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
operator|.
name|Entry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseConfiguration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|net
operator|.
name|DNS
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|StringUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|server
operator|.
name|ServerConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|server
operator|.
name|ZooKeeperServerMain
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|server
operator|.
name|quorum
operator|.
name|QuorumPeerConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|server
operator|.
name|quorum
operator|.
name|QuorumPeerMain
import|;
end_import

begin_comment
comment|/**  * HBase's version of ZooKeeper's QuorumPeer. When HBase is set to manage  * ZooKeeper, this class is used to start up QuorumPeer instances. By doing  * things in here rather than directly calling to ZooKeeper, we have more  * control over the process. This class uses {@link ZKConfig} to parse the  * zoo.cfg and inject variables from HBase's site.xml configuration in.  */
end_comment

begin_class
specifier|public
class|class
name|HQuorumPeer
block|{
comment|/**    * Parse ZooKeeper configuration from HBase XML config and run a QuorumPeer.    * @param args String[] of command line arguments. Not used.    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
try|try
block|{
name|Properties
name|zkProperties
init|=
name|ZKConfig
operator|.
name|makeZKProps
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|writeMyID
argument_list|(
name|zkProperties
argument_list|)
expr_stmt|;
name|QuorumPeerConfig
name|zkConfig
init|=
operator|new
name|QuorumPeerConfig
argument_list|()
decl_stmt|;
name|zkConfig
operator|.
name|parseProperties
argument_list|(
name|zkProperties
argument_list|)
expr_stmt|;
name|runZKServer
argument_list|(
name|zkConfig
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|void
name|runZKServer
parameter_list|(
name|QuorumPeerConfig
name|zkConfig
parameter_list|)
throws|throws
name|UnknownHostException
throws|,
name|IOException
block|{
if|if
condition|(
name|zkConfig
operator|.
name|isDistributed
argument_list|()
condition|)
block|{
name|QuorumPeerMain
name|qp
init|=
operator|new
name|QuorumPeerMain
argument_list|()
decl_stmt|;
name|qp
operator|.
name|runFromConfig
argument_list|(
name|zkConfig
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|ZooKeeperServerMain
name|zk
init|=
operator|new
name|ZooKeeperServerMain
argument_list|()
decl_stmt|;
name|ServerConfig
name|serverConfig
init|=
operator|new
name|ServerConfig
argument_list|()
decl_stmt|;
name|serverConfig
operator|.
name|readFrom
argument_list|(
name|zkConfig
argument_list|)
expr_stmt|;
name|zk
operator|.
name|runFromConfig
argument_list|(
name|serverConfig
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
specifier|static
name|boolean
name|addressIsLocalHost
parameter_list|(
name|String
name|address
parameter_list|)
block|{
return|return
name|address
operator|.
name|equals
argument_list|(
literal|"localhost"
argument_list|)
operator|||
name|address
operator|.
name|equals
argument_list|(
literal|"127.0.0.1"
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|void
name|writeMyID
parameter_list|(
name|Properties
name|properties
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|myId
init|=
operator|-
literal|1
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|String
name|myAddress
init|=
name|DNS
operator|.
name|getDefaultHost
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.zookeeper.dns.interface"
argument_list|,
literal|"default"
argument_list|)
argument_list|,
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.zookeeper.dns.nameserver"
argument_list|,
literal|"default"
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|ips
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// Add what could be the best (configured) match
name|ips
operator|.
name|add
argument_list|(
name|myAddress
operator|.
name|contains
argument_list|(
literal|"."
argument_list|)
condition|?
name|myAddress
else|:
name|StringUtils
operator|.
name|simpleHostname
argument_list|(
name|myAddress
argument_list|)
argument_list|)
expr_stmt|;
comment|// For all nics get all hostnames and IPs
name|Enumeration
argument_list|<
name|?
argument_list|>
name|nics
init|=
name|NetworkInterface
operator|.
name|getNetworkInterfaces
argument_list|()
decl_stmt|;
while|while
condition|(
name|nics
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|Enumeration
argument_list|<
name|?
argument_list|>
name|rawAdrs
init|=
operator|(
operator|(
name|NetworkInterface
operator|)
name|nics
operator|.
name|nextElement
argument_list|()
operator|)
operator|.
name|getInetAddresses
argument_list|()
decl_stmt|;
while|while
condition|(
name|rawAdrs
operator|.
name|hasMoreElements
argument_list|()
condition|)
block|{
name|InetAddress
name|inet
init|=
operator|(
name|InetAddress
operator|)
name|rawAdrs
operator|.
name|nextElement
argument_list|()
decl_stmt|;
name|ips
operator|.
name|add
argument_list|(
name|StringUtils
operator|.
name|simpleHostname
argument_list|(
name|inet
operator|.
name|getHostName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|ips
operator|.
name|add
argument_list|(
name|inet
operator|.
name|getHostAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|properties
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|String
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|trim
argument_list|()
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
literal|"server."
argument_list|)
condition|)
block|{
name|int
name|dot
init|=
name|key
operator|.
name|indexOf
argument_list|(
literal|'.'
argument_list|)
decl_stmt|;
name|long
name|id
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|key
operator|.
name|substring
argument_list|(
name|dot
operator|+
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
index|[]
name|parts
init|=
name|value
operator|.
name|split
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
name|String
name|address
init|=
name|parts
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|addressIsLocalHost
argument_list|(
name|address
argument_list|)
operator|||
name|ips
operator|.
name|contains
argument_list|(
name|address
argument_list|)
condition|)
block|{
name|myId
operator|=
name|id
expr_stmt|;
break|break;
block|}
block|}
block|}
if|if
condition|(
name|myId
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Could not find my address: "
operator|+
name|myAddress
operator|+
literal|" in list of ZooKeeper quorum servers"
argument_list|)
throw|;
block|}
name|String
name|dataDirStr
init|=
name|properties
operator|.
name|get
argument_list|(
literal|"dataDir"
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|trim
argument_list|()
decl_stmt|;
name|File
name|dataDir
init|=
operator|new
name|File
argument_list|(
name|dataDirStr
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|dataDir
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
if|if
condition|(
operator|!
name|dataDir
operator|.
name|mkdirs
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unable to create data dir "
operator|+
name|dataDir
argument_list|)
throw|;
block|}
block|}
name|File
name|myIdFile
init|=
operator|new
name|File
argument_list|(
name|dataDir
argument_list|,
literal|"myid"
argument_list|)
decl_stmt|;
name|PrintWriter
name|w
init|=
operator|new
name|PrintWriter
argument_list|(
name|myIdFile
argument_list|)
decl_stmt|;
name|w
operator|.
name|println
argument_list|(
name|myId
argument_list|)
expr_stmt|;
name|w
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

