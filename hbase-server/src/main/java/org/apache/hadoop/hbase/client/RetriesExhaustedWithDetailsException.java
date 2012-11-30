begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|DoNotRetryIOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * This subclass of {@link org.apache.hadoop.hbase.client.RetriesExhaustedException}  * is thrown when we have more information about which rows were causing which  * exceptions on what servers.  You can call {@link #mayHaveClusterIssues()}  * and if the result is false, you have input error problems, otherwise you  * may have cluster issues.  You can iterate over the causes, rows and last  * known server addresses via {@link #getNumExceptions()} and  * {@link #getCause(int)}, {@link #getRow(int)} and {@link #getHostnamePort(int)}.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|RetriesExhaustedWithDetailsException
extends|extends
name|RetriesExhaustedException
block|{
name|List
argument_list|<
name|Throwable
argument_list|>
name|exceptions
decl_stmt|;
name|List
argument_list|<
name|Row
argument_list|>
name|actions
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|hostnameAndPort
decl_stmt|;
specifier|public
name|RetriesExhaustedWithDetailsException
parameter_list|(
name|List
argument_list|<
name|Throwable
argument_list|>
name|exceptions
parameter_list|,
name|List
argument_list|<
name|Row
argument_list|>
name|actions
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|hostnameAndPort
parameter_list|)
block|{
name|super
argument_list|(
literal|"Failed "
operator|+
name|exceptions
operator|.
name|size
argument_list|()
operator|+
literal|" action"
operator|+
name|pluralize
argument_list|(
name|exceptions
argument_list|)
operator|+
literal|": "
operator|+
name|getDesc
argument_list|(
name|exceptions
argument_list|,
name|actions
argument_list|,
name|hostnameAndPort
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|exceptions
operator|=
name|exceptions
expr_stmt|;
name|this
operator|.
name|actions
operator|=
name|actions
expr_stmt|;
name|this
operator|.
name|hostnameAndPort
operator|=
name|hostnameAndPort
expr_stmt|;
block|}
specifier|public
name|List
argument_list|<
name|Throwable
argument_list|>
name|getCauses
parameter_list|()
block|{
return|return
name|exceptions
return|;
block|}
specifier|public
name|int
name|getNumExceptions
parameter_list|()
block|{
return|return
name|exceptions
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|Throwable
name|getCause
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|exceptions
operator|.
name|get
argument_list|(
name|i
argument_list|)
return|;
block|}
specifier|public
name|Row
name|getRow
parameter_list|(
name|int
name|i
parameter_list|)
block|{
return|return
name|actions
operator|.
name|get
argument_list|(
name|i
argument_list|)
return|;
block|}
specifier|public
name|String
name|getHostnamePort
parameter_list|(
specifier|final
name|int
name|i
parameter_list|)
block|{
return|return
name|this
operator|.
name|hostnameAndPort
operator|.
name|get
argument_list|(
name|i
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|mayHaveClusterIssues
parameter_list|()
block|{
name|boolean
name|res
init|=
literal|false
decl_stmt|;
comment|// If all of the exceptions are DNRIOE not exception
for|for
control|(
name|Throwable
name|t
range|:
name|exceptions
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|t
operator|instanceof
name|DoNotRetryIOException
operator|)
condition|)
block|{
name|res
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
name|res
return|;
block|}
specifier|public
specifier|static
name|String
name|pluralize
parameter_list|(
name|Collection
argument_list|<
name|?
argument_list|>
name|c
parameter_list|)
block|{
return|return
name|pluralize
argument_list|(
name|c
operator|.
name|size
argument_list|()
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|String
name|pluralize
parameter_list|(
name|int
name|c
parameter_list|)
block|{
return|return
name|c
operator|>
literal|1
condition|?
literal|"s"
else|:
literal|""
return|;
block|}
specifier|public
specifier|static
name|String
name|getDesc
parameter_list|(
name|List
argument_list|<
name|Throwable
argument_list|>
name|exceptions
parameter_list|,
name|List
argument_list|<
name|Row
argument_list|>
name|actions
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|hostnamePort
parameter_list|)
block|{
name|String
name|s
init|=
name|getDesc
argument_list|(
name|classifyExs
argument_list|(
name|exceptions
argument_list|)
argument_list|)
decl_stmt|;
name|StringBuilder
name|addrs
init|=
operator|new
name|StringBuilder
argument_list|(
name|s
argument_list|)
decl_stmt|;
name|addrs
operator|.
name|append
argument_list|(
literal|"servers with issues: "
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|uniqAddr
init|=
operator|new
name|HashSet
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
name|uniqAddr
operator|.
name|addAll
argument_list|(
name|hostnamePort
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|addr
range|:
name|uniqAddr
control|)
block|{
name|addrs
operator|.
name|append
argument_list|(
name|addr
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
return|return
name|s
return|;
block|}
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|classifyExs
parameter_list|(
name|List
argument_list|<
name|Throwable
argument_list|>
name|ths
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|cls
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Throwable
name|t
range|:
name|ths
control|)
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
continue|continue;
name|String
name|name
init|=
literal|""
decl_stmt|;
if|if
condition|(
name|t
operator|instanceof
name|DoNotRetryIOException
condition|)
block|{
name|name
operator|=
name|t
operator|.
name|getMessage
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|name
operator|=
name|t
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
expr_stmt|;
block|}
name|Integer
name|i
init|=
name|cls
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|null
condition|)
block|{
name|i
operator|=
literal|0
expr_stmt|;
block|}
name|i
operator|+=
literal|1
expr_stmt|;
name|cls
operator|.
name|put
argument_list|(
name|name
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|cls
return|;
block|}
specifier|public
specifier|static
name|String
name|getDesc
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|classificaton
parameter_list|)
block|{
name|StringBuilder
name|classificatons
init|=
operator|new
name|StringBuilder
argument_list|(
literal|11
argument_list|)
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Integer
argument_list|>
name|e
range|:
name|classificaton
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|classificatons
operator|.
name|append
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|classificatons
operator|.
name|append
argument_list|(
literal|": "
argument_list|)
expr_stmt|;
name|classificatons
operator|.
name|append
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|classificatons
operator|.
name|append
argument_list|(
literal|" time"
argument_list|)
expr_stmt|;
name|classificatons
operator|.
name|append
argument_list|(
name|pluralize
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|classificatons
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
return|return
name|classificatons
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

