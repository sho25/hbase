begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|thrift
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|thrift
operator|.
name|Constants
operator|.
name|SERVER_TYPE_CONF_KEY
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
name|Arrays
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
name|thrift
operator|.
name|server
operator|.
name|THsHaServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TNonblockingServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TThreadedSelectorServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Option
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|OptionGroup
import|;
end_import

begin_comment
comment|/** An enum of server implementation selections */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
enum|enum
name|ImplType
block|{
name|HS_HA
argument_list|(
literal|"hsha"
argument_list|,
literal|true
argument_list|,
name|THsHaServer
operator|.
name|class
argument_list|,
literal|true
argument_list|)
block|,
name|NONBLOCKING
argument_list|(
literal|"nonblocking"
argument_list|,
literal|true
argument_list|,
name|TNonblockingServer
operator|.
name|class
argument_list|,
literal|true
argument_list|)
block|,
name|THREAD_POOL
argument_list|(
literal|"threadpool"
argument_list|,
literal|false
argument_list|,
name|TBoundedThreadPoolServer
operator|.
name|class
argument_list|,
literal|true
argument_list|)
block|,
name|THREADED_SELECTOR
argument_list|(
literal|"threadedselector"
argument_list|,
literal|true
argument_list|,
name|TThreadedSelectorServer
operator|.
name|class
argument_list|,
literal|true
argument_list|)
block|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|ImplType
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ImplType
name|DEFAULT
init|=
name|THREAD_POOL
decl_stmt|;
specifier|final
name|String
name|option
decl_stmt|;
specifier|final
name|boolean
name|isAlwaysFramed
decl_stmt|;
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|TServer
argument_list|>
name|serverClass
decl_stmt|;
specifier|final
name|boolean
name|canSpecifyBindIP
decl_stmt|;
specifier|private
name|ImplType
parameter_list|(
name|String
name|option
parameter_list|,
name|boolean
name|isAlwaysFramed
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|TServer
argument_list|>
name|serverClass
parameter_list|,
name|boolean
name|canSpecifyBindIP
parameter_list|)
block|{
name|this
operator|.
name|option
operator|=
name|option
expr_stmt|;
name|this
operator|.
name|isAlwaysFramed
operator|=
name|isAlwaysFramed
expr_stmt|;
name|this
operator|.
name|serverClass
operator|=
name|serverClass
expr_stmt|;
name|this
operator|.
name|canSpecifyBindIP
operator|=
name|canSpecifyBindIP
expr_stmt|;
block|}
comment|/**    * @return<code>-option</code>    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"-"
operator|+
name|option
return|;
block|}
specifier|public
name|String
name|getOption
parameter_list|()
block|{
return|return
name|option
return|;
block|}
specifier|public
name|boolean
name|isAlwaysFramed
parameter_list|()
block|{
return|return
name|isAlwaysFramed
return|;
block|}
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Use the "
operator|+
name|serverClass
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|isAlwaysFramed
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|" This implies the framed transport."
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|==
name|DEFAULT
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"This is the default."
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|static
name|OptionGroup
name|createOptionGroup
parameter_list|()
block|{
name|OptionGroup
name|group
init|=
operator|new
name|OptionGroup
argument_list|()
decl_stmt|;
for|for
control|(
name|ImplType
name|t
range|:
name|values
argument_list|()
control|)
block|{
name|group
operator|.
name|addOption
argument_list|(
operator|new
name|Option
argument_list|(
name|t
operator|.
name|option
argument_list|,
name|t
operator|.
name|getDescription
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|group
return|;
block|}
specifier|public
specifier|static
name|ImplType
name|getServerImpl
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|confType
init|=
name|conf
operator|.
name|get
argument_list|(
name|SERVER_TYPE_CONF_KEY
argument_list|,
name|THREAD_POOL
operator|.
name|option
argument_list|)
decl_stmt|;
for|for
control|(
name|ImplType
name|t
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|confType
operator|.
name|equals
argument_list|(
name|t
operator|.
name|option
argument_list|)
condition|)
block|{
return|return
name|t
return|;
block|}
block|}
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unknown server ImplType.option:"
operator|+
name|confType
argument_list|)
throw|;
block|}
specifier|static
name|void
name|setServerImpl
parameter_list|(
name|CommandLine
name|cmd
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|ImplType
name|chosenType
init|=
literal|null
decl_stmt|;
name|int
name|numChosen
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ImplType
name|t
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|cmd
operator|.
name|hasOption
argument_list|(
name|t
operator|.
name|option
argument_list|)
condition|)
block|{
name|chosenType
operator|=
name|t
expr_stmt|;
operator|++
name|numChosen
expr_stmt|;
block|}
block|}
if|if
condition|(
name|numChosen
operator|<
literal|1
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Using default thrift server type"
argument_list|)
expr_stmt|;
name|chosenType
operator|=
name|DEFAULT
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|numChosen
operator|>
literal|1
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Exactly one option out of "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|values
argument_list|()
argument_list|)
operator|+
literal|" has to be specified"
argument_list|)
throw|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Using thrift server type "
operator|+
name|chosenType
operator|.
name|option
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|SERVER_TYPE_CONF_KEY
argument_list|,
name|chosenType
operator|.
name|option
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|simpleClassName
parameter_list|()
block|{
return|return
name|serverClass
operator|.
name|getSimpleName
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|List
argument_list|<
name|String
argument_list|>
name|serversThatCannotSpecifyBindIP
parameter_list|()
block|{
name|List
argument_list|<
name|String
argument_list|>
name|l
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|ImplType
name|t
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|t
operator|.
name|canSpecifyBindIP
condition|)
block|{
name|l
operator|.
name|add
argument_list|(
name|t
operator|.
name|simpleClassName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|l
return|;
block|}
block|}
end_enum

end_unit

