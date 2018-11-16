begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Iterator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
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
name|procedure2
operator|.
name|Procedure
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
name|com
operator|.
name|google
operator|.
name|gson
operator|.
name|JsonArray
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
name|com
operator|.
name|google
operator|.
name|gson
operator|.
name|JsonElement
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
name|com
operator|.
name|google
operator|.
name|gson
operator|.
name|JsonObject
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
name|com
operator|.
name|google
operator|.
name|gson
operator|.
name|JsonParser
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ProcedureProtos
operator|.
name|ProcedureState
import|;
end_import

begin_class
specifier|public
specifier|final
class|class
name|ProcedureTestUtil
block|{
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
name|ProcedureTestUtil
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ProcedureTestUtil
parameter_list|()
block|{   }
specifier|private
specifier|static
name|Optional
argument_list|<
name|JsonObject
argument_list|>
name|getProcedure
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Procedure
argument_list|<
name|?
argument_list|>
argument_list|>
name|clazz
parameter_list|,
name|JsonParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|JsonArray
name|array
init|=
name|parser
operator|.
name|parse
argument_list|(
name|util
operator|.
name|getAdmin
argument_list|()
operator|.
name|getProcedures
argument_list|()
argument_list|)
operator|.
name|getAsJsonArray
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|JsonElement
argument_list|>
name|iterator
init|=
name|array
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|JsonElement
name|element
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|JsonObject
name|obj
init|=
name|element
operator|.
name|getAsJsonObject
argument_list|()
decl_stmt|;
name|String
name|className
init|=
name|obj
operator|.
name|get
argument_list|(
literal|"className"
argument_list|)
operator|.
name|getAsString
argument_list|()
decl_stmt|;
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
name|clazz
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|Optional
operator|.
name|of
argument_list|(
name|obj
argument_list|)
return|;
block|}
block|}
return|return
name|Optional
operator|.
name|empty
argument_list|()
return|;
block|}
specifier|public
specifier|static
name|void
name|waitUntilProcedureWaitingTimeout
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Procedure
argument_list|<
name|?
argument_list|>
argument_list|>
name|clazz
parameter_list|,
name|long
name|timeout
parameter_list|)
throws|throws
name|IOException
block|{
name|JsonParser
name|parser
init|=
operator|new
name|JsonParser
argument_list|()
decl_stmt|;
name|util
operator|.
name|waitFor
argument_list|(
name|timeout
argument_list|,
parameter_list|()
lambda|->
name|getProcedure
argument_list|(
name|util
argument_list|,
name|clazz
argument_list|,
name|parser
argument_list|)
operator|.
name|filter
argument_list|(
name|o
lambda|->
name|ProcedureState
operator|.
name|WAITING_TIMEOUT
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|o
operator|.
name|get
argument_list|(
literal|"state"
argument_list|)
operator|.
name|getAsString
argument_list|()
argument_list|)
argument_list|)
operator|.
name|isPresent
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|waitUntilProcedureTimeoutIncrease
parameter_list|(
name|HBaseTestingUtility
name|util
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Procedure
argument_list|<
name|?
argument_list|>
argument_list|>
name|clazz
parameter_list|,
name|int
name|times
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
name|JsonParser
name|parser
init|=
operator|new
name|JsonParser
argument_list|()
decl_stmt|;
name|long
name|oldTimeout
init|=
literal|0
decl_stmt|;
name|int
name|timeoutIncrements
init|=
literal|0
decl_stmt|;
for|for
control|(
init|;
condition|;
control|)
block|{
name|long
name|timeout
init|=
name|getProcedure
argument_list|(
name|util
argument_list|,
name|clazz
argument_list|,
name|parser
argument_list|)
operator|.
name|filter
argument_list|(
name|o
lambda|->
name|o
operator|.
name|has
argument_list|(
literal|"timeout"
argument_list|)
argument_list|)
operator|.
name|map
argument_list|(
name|o
lambda|->
name|o
operator|.
name|get
argument_list|(
literal|"timeout"
argument_list|)
operator|.
name|getAsLong
argument_list|()
argument_list|)
operator|.
name|orElse
argument_list|(
operator|-
literal|1L
argument_list|)
decl_stmt|;
if|if
condition|(
name|timeout
operator|>
name|oldTimeout
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Timeout incremented, was {}, now is {}, increments={}"
argument_list|,
name|timeout
argument_list|,
name|oldTimeout
argument_list|,
name|timeoutIncrements
argument_list|)
expr_stmt|;
name|oldTimeout
operator|=
name|timeout
expr_stmt|;
name|timeoutIncrements
operator|++
expr_stmt|;
if|if
condition|(
name|timeoutIncrements
operator|>
name|times
condition|)
block|{
break|break;
block|}
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
