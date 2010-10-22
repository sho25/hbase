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

begin_comment
comment|/**  * Tool for reading a ZooKeeper server from HBase XML configuration producing  * the '-server host:port' argument to pass ZooKeeperMain.  This program  * emits either '-server HOST:PORT" where HOST is one of the zk ensemble  * members plus zk client port OR it emits '' if no zk servers found (Yes,  * it emits '-server' too).  */
end_comment

begin_class
specifier|public
class|class
name|ZooKeeperMainServerArg
block|{
specifier|public
name|String
name|parse
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|)
block|{
comment|// Note that we do not simply grab the property
comment|// HConstants.ZOOKEEPER_QUORUM from the HBaseConfiguration because the
comment|// user may be using a zoo.cfg file.
name|Properties
name|zkProps
init|=
name|ZKConfig
operator|.
name|makeZKProps
argument_list|(
name|c
argument_list|)
decl_stmt|;
name|String
name|host
init|=
literal|null
decl_stmt|;
name|String
name|clientPort
init|=
literal|null
decl_stmt|;
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
name|zkProps
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
operator|&&
name|host
operator|==
literal|null
condition|)
block|{
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
name|host
operator|=
name|parts
index|[
literal|0
index|]
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|key
operator|.
name|endsWith
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
if|if
condition|(
name|host
operator|!=
literal|null
operator|&&
name|clientPort
operator|!=
literal|null
condition|)
break|break;
block|}
return|return
name|host
operator|!=
literal|null
operator|&&
name|clientPort
operator|!=
literal|null
condition|?
name|host
operator|+
literal|":"
operator|+
name|clientPort
else|:
literal|null
return|;
block|}
comment|/**    * Run the tool.    * @param args Command line arguments. First arg is path to zookeepers file.    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
name|args
index|[]
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
name|String
name|hostport
init|=
operator|new
name|ZooKeeperMainServerArg
argument_list|()
operator|.
name|parse
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
operator|(
name|hostport
operator|==
literal|null
operator|||
name|hostport
operator|.
name|length
argument_list|()
operator|==
literal|0
operator|)
condition|?
literal|""
else|:
literal|"-server "
operator|+
name|hostport
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

