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
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
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
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|HConstants
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

begin_comment
comment|/**  * Utility methods for reading, parsing, and building zookeeper configuration.  */
end_comment

begin_class
specifier|public
class|class
name|ZKConfig
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ZKConfig
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VARIABLE_START
init|=
literal|"${"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|VARIABLE_START_LENGTH
init|=
name|VARIABLE_START
operator|.
name|length
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|VARIABLE_END
init|=
literal|"}"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|VARIABLE_END_LENGTH
init|=
name|VARIABLE_END
operator|.
name|length
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ZK_CFG_PROPERTY
init|=
literal|"hbase.zookeeper.property."
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|ZK_CFG_PROPERTY_SIZE
init|=
name|ZK_CFG_PROPERTY
operator|.
name|length
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ZK_CLIENT_PORT_KEY
init|=
name|ZK_CFG_PROPERTY
operator|+
literal|"clientPort"
decl_stmt|;
comment|/**    * Make a Properties object holding ZooKeeper config equivalent to zoo.cfg.    * If there is a zoo.cfg in the classpath, simply read it in. Otherwise parse    * the corresponding config options from the HBase XML configs and generate    * the appropriate ZooKeeper properties.    * @param conf Configuration to read from.    * @return Properties holding mappings representing ZooKeeper zoo.cfg file.    */
specifier|public
specifier|static
name|Properties
name|makeZKProps
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// First check if there is a zoo.cfg in the CLASSPATH. If so, simply read
comment|// it and grab its configuration properties.
name|ClassLoader
name|cl
init|=
name|HQuorumPeer
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
specifier|final
name|InputStream
name|inputStream
init|=
name|cl
operator|.
name|getResourceAsStream
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CONFIG_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|inputStream
operator|!=
literal|null
condition|)
block|{
try|try
block|{
return|return
name|parseZooCfg
argument_list|(
name|conf
argument_list|,
name|inputStream
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Cannot read "
operator|+
name|HConstants
operator|.
name|ZOOKEEPER_CONFIG_NAME
operator|+
literal|", loading from XML files"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Otherwise, use the configuration options from HBase's XML files.
name|Properties
name|zkProperties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
comment|// Directly map all of the hbase.zookeeper.property.KEY properties.
for|for
control|(
name|Entry
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|entry
range|:
name|conf
control|)
block|{
name|String
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|startsWith
argument_list|(
name|ZK_CFG_PROPERTY
argument_list|)
condition|)
block|{
name|String
name|zkKey
init|=
name|key
operator|.
name|substring
argument_list|(
name|ZK_CFG_PROPERTY_SIZE
argument_list|)
decl_stmt|;
name|String
name|value
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
comment|// If the value has variables substitutions, need to do a get.
if|if
condition|(
name|value
operator|.
name|contains
argument_list|(
name|VARIABLE_START
argument_list|)
condition|)
block|{
name|value
operator|=
name|conf
operator|.
name|get
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
name|zkProperties
operator|.
name|put
argument_list|(
name|zkKey
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|// If clientPort is not set, assign the default
if|if
condition|(
name|zkProperties
operator|.
name|getProperty
argument_list|(
name|ZK_CLIENT_PORT_KEY
argument_list|)
operator|==
literal|null
condition|)
block|{
name|zkProperties
operator|.
name|put
argument_list|(
name|ZK_CLIENT_PORT_KEY
argument_list|,
name|HConstants
operator|.
name|DEFAULT_ZOOKEPER_CLIENT_PORT
argument_list|)
expr_stmt|;
block|}
comment|// Create the server.X properties.
name|int
name|peerPort
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.zookeeper.peerport"
argument_list|,
literal|2888
argument_list|)
decl_stmt|;
name|int
name|leaderPort
init|=
name|conf
operator|.
name|getInt
argument_list|(
literal|"hbase.zookeeper.leaderport"
argument_list|,
literal|3888
argument_list|)
decl_stmt|;
specifier|final
name|String
index|[]
name|serverHosts
init|=
name|conf
operator|.
name|getStrings
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_QUORUM
argument_list|,
literal|"localhost"
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|serverHosts
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|String
name|serverHost
init|=
name|serverHosts
index|[
name|i
index|]
decl_stmt|;
name|String
name|address
init|=
name|serverHost
operator|+
literal|":"
operator|+
name|peerPort
operator|+
literal|":"
operator|+
name|leaderPort
decl_stmt|;
name|String
name|key
init|=
literal|"server."
operator|+
name|i
decl_stmt|;
name|zkProperties
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|address
argument_list|)
expr_stmt|;
block|}
return|return
name|zkProperties
return|;
block|}
comment|/**    * Parse ZooKeeper's zoo.cfg, injecting HBase Configuration variables in.    * This method is used for testing so we can pass our own InputStream.    * @param conf HBaseConfiguration to use for injecting variables.    * @param inputStream InputStream to read from.    * @return Properties parsed from config stream with variables substituted.    * @throws IOException if anything goes wrong parsing config    */
specifier|public
specifier|static
name|Properties
name|parseZooCfg
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|InputStream
name|inputStream
parameter_list|)
throws|throws
name|IOException
block|{
name|Properties
name|properties
init|=
operator|new
name|Properties
argument_list|()
decl_stmt|;
try|try
block|{
name|properties
operator|.
name|load
argument_list|(
name|inputStream
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
specifier|final
name|String
name|msg
init|=
literal|"fail to read properties from "
operator|+
name|HConstants
operator|.
name|ZOOKEEPER_CONFIG_NAME
decl_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
throw|;
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
name|StringBuilder
name|newValue
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
name|varStart
init|=
name|value
operator|.
name|indexOf
argument_list|(
name|VARIABLE_START
argument_list|)
decl_stmt|;
name|int
name|varEnd
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|varStart
operator|!=
operator|-
literal|1
condition|)
block|{
name|varEnd
operator|=
name|value
operator|.
name|indexOf
argument_list|(
name|VARIABLE_END
argument_list|,
name|varStart
argument_list|)
expr_stmt|;
if|if
condition|(
name|varEnd
operator|==
operator|-
literal|1
condition|)
block|{
name|String
name|msg
init|=
literal|"variable at "
operator|+
name|varStart
operator|+
literal|" has no end marker"
decl_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|String
name|variable
init|=
name|value
operator|.
name|substring
argument_list|(
name|varStart
operator|+
name|VARIABLE_START_LENGTH
argument_list|,
name|varEnd
argument_list|)
decl_stmt|;
name|String
name|substituteValue
init|=
name|System
operator|.
name|getProperty
argument_list|(
name|variable
argument_list|)
decl_stmt|;
if|if
condition|(
name|substituteValue
operator|==
literal|null
condition|)
block|{
name|substituteValue
operator|=
name|conf
operator|.
name|get
argument_list|(
name|variable
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|substituteValue
operator|==
literal|null
condition|)
block|{
name|String
name|msg
init|=
literal|"variable "
operator|+
name|variable
operator|+
literal|" not set in system property "
operator|+
literal|"or hbase configs"
decl_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
name|newValue
operator|.
name|append
argument_list|(
name|substituteValue
argument_list|)
expr_stmt|;
name|varEnd
operator|+=
name|VARIABLE_END_LENGTH
expr_stmt|;
name|varStart
operator|=
name|value
operator|.
name|indexOf
argument_list|(
name|VARIABLE_START
argument_list|,
name|varEnd
argument_list|)
expr_stmt|;
block|}
comment|// Special case for 'hbase.cluster.distributed' property being 'true'
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
if|if
condition|(
name|conf
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|CLUSTER_DISTRIBUTED
argument_list|)
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|CLUSTER_IS_DISTRIBUTED
argument_list|)
operator|&&
name|value
operator|.
name|startsWith
argument_list|(
literal|"localhost"
argument_list|)
condition|)
block|{
name|String
name|msg
init|=
literal|"The server in zoo.cfg cannot be set to localhost "
operator|+
literal|"in a fully-distributed setup because it won't be reachable. "
operator|+
literal|"See \"Getting Started\" for more information."
decl_stmt|;
name|LOG
operator|.
name|fatal
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
block|}
name|newValue
operator|.
name|append
argument_list|(
name|value
operator|.
name|substring
argument_list|(
name|varEnd
argument_list|)
argument_list|)
expr_stmt|;
name|properties
operator|.
name|setProperty
argument_list|(
name|key
argument_list|,
name|newValue
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|properties
return|;
block|}
comment|/**    * Return the ZK Quorum servers string given zk properties returned by    * makeZKProps    * @param properties    * @return Quorum servers String    */
specifier|public
specifier|static
name|String
name|getZKQuorumServersString
parameter_list|(
name|Properties
name|properties
parameter_list|)
block|{
name|String
name|clientPort
init|=
literal|null
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|servers
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
comment|// The clientPort option may come after the server.X hosts, so we need to
comment|// grab everything and then create the final host:port comma separated list.
name|boolean
name|anyValid
init|=
literal|false
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|Object
argument_list|,
name|Object
argument_list|>
name|property
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
name|property
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
name|property
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
name|equals
argument_list|(
literal|"clientPort"
argument_list|)
condition|)
block|{
name|clientPort
operator|=
name|value
expr_stmt|;
block|}
elseif|else
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
name|String
name|host
init|=
name|value
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|value
operator|.
name|indexOf
argument_list|(
literal|':'
argument_list|)
argument_list|)
decl_stmt|;
name|servers
operator|.
name|add
argument_list|(
name|host
argument_list|)
expr_stmt|;
try|try
block|{
comment|//noinspection ResultOfMethodCallIgnored
name|InetAddress
operator|.
name|getByName
argument_list|(
name|host
argument_list|)
expr_stmt|;
name|anyValid
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|e
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|anyValid
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"no valid quorum servers found in "
operator|+
name|HConstants
operator|.
name|ZOOKEEPER_CONFIG_NAME
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|clientPort
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"no clientPort found in "
operator|+
name|HConstants
operator|.
name|ZOOKEEPER_CONFIG_NAME
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
if|if
condition|(
name|servers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"No server.X lines found in conf/zoo.cfg. HBase must have a "
operator|+
literal|"ZooKeeper cluster configured for its operation."
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|StringBuilder
name|hostPortBuilder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|servers
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|String
name|host
init|=
name|servers
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|hostPortBuilder
operator|.
name|append
argument_list|(
literal|','
argument_list|)
expr_stmt|;
block|}
name|hostPortBuilder
operator|.
name|append
argument_list|(
name|host
argument_list|)
expr_stmt|;
name|hostPortBuilder
operator|.
name|append
argument_list|(
literal|':'
argument_list|)
expr_stmt|;
name|hostPortBuilder
operator|.
name|append
argument_list|(
name|clientPort
argument_list|)
expr_stmt|;
block|}
return|return
name|hostPortBuilder
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    * Return the ZK Quorum servers string given the specified configuration.    * @param properties    * @return Quorum servers    */
specifier|public
specifier|static
name|String
name|getZKQuorumServersString
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
return|return
name|getZKQuorumServersString
argument_list|(
name|makeZKProps
argument_list|(
name|conf
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

