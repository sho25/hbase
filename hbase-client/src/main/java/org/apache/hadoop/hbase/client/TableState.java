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
name|hbase
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
name|TableName
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
import|;
end_import

begin_comment
comment|/**  * Represents table state.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|TableState
block|{
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|static
enum|enum
name|State
block|{
name|ENABLED
block|,
name|DISABLED
block|,
name|DISABLING
block|,
name|ENABLING
block|;
comment|/**      * Covert from PB version of State      *      * @param state convert from      * @return POJO      */
specifier|public
specifier|static
name|State
name|convert
parameter_list|(
name|HBaseProtos
operator|.
name|TableState
operator|.
name|State
name|state
parameter_list|)
block|{
name|State
name|ret
decl_stmt|;
switch|switch
condition|(
name|state
condition|)
block|{
case|case
name|ENABLED
case|:
name|ret
operator|=
name|State
operator|.
name|ENABLED
expr_stmt|;
break|break;
case|case
name|DISABLED
case|:
name|ret
operator|=
name|State
operator|.
name|DISABLED
expr_stmt|;
break|break;
case|case
name|DISABLING
case|:
name|ret
operator|=
name|State
operator|.
name|DISABLING
expr_stmt|;
break|break;
case|case
name|ENABLING
case|:
name|ret
operator|=
name|State
operator|.
name|ENABLING
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|state
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|ret
return|;
block|}
comment|/**      * Covert to PB version of State      *      * @return PB      */
specifier|public
name|HBaseProtos
operator|.
name|TableState
operator|.
name|State
name|convert
parameter_list|()
block|{
name|HBaseProtos
operator|.
name|TableState
operator|.
name|State
name|state
decl_stmt|;
switch|switch
condition|(
name|this
condition|)
block|{
case|case
name|ENABLED
case|:
name|state
operator|=
name|HBaseProtos
operator|.
name|TableState
operator|.
name|State
operator|.
name|ENABLED
expr_stmt|;
break|break;
case|case
name|DISABLED
case|:
name|state
operator|=
name|HBaseProtos
operator|.
name|TableState
operator|.
name|State
operator|.
name|DISABLED
expr_stmt|;
break|break;
case|case
name|DISABLING
case|:
name|state
operator|=
name|HBaseProtos
operator|.
name|TableState
operator|.
name|State
operator|.
name|DISABLING
expr_stmt|;
break|break;
case|case
name|ENABLING
case|:
name|state
operator|=
name|HBaseProtos
operator|.
name|TableState
operator|.
name|State
operator|.
name|ENABLING
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|this
operator|.
name|toString
argument_list|()
argument_list|)
throw|;
block|}
return|return
name|state
return|;
block|}
block|}
specifier|private
specifier|final
name|long
name|timestamp
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|State
name|state
decl_stmt|;
comment|/**    * Create instance of TableState.    * @param state table state    */
specifier|public
name|TableState
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|State
name|state
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
block|}
comment|/**    * Create instance of TableState with current timestamp    *    * @param tableName table for which state is created    * @param state     state of the table    */
specifier|public
name|TableState
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|State
name|state
parameter_list|)
block|{
name|this
argument_list|(
name|tableName
argument_list|,
name|state
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return table state    */
specifier|public
name|State
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
comment|/**    * Timestamp of table state    *    * @return milliseconds    */
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
comment|/**    * Table name for state    *    * @return milliseconds    */
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|tableName
return|;
block|}
comment|/**    * Check that table in given states    * @param state state    * @return true if satisfies    */
specifier|public
name|boolean
name|inStates
parameter_list|(
name|State
name|state
parameter_list|)
block|{
return|return
name|this
operator|.
name|state
operator|.
name|equals
argument_list|(
name|state
argument_list|)
return|;
block|}
comment|/**    * Check that table in given states    * @param states state list    * @return true if satisfies    */
specifier|public
name|boolean
name|inStates
parameter_list|(
name|State
modifier|...
name|states
parameter_list|)
block|{
for|for
control|(
name|State
name|s
range|:
name|states
control|)
block|{
if|if
condition|(
name|s
operator|.
name|equals
argument_list|(
name|this
operator|.
name|state
argument_list|)
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**    * Covert to PB version of TableState    * @return PB    */
specifier|public
name|HBaseProtos
operator|.
name|TableState
name|convert
parameter_list|()
block|{
return|return
name|HBaseProtos
operator|.
name|TableState
operator|.
name|newBuilder
argument_list|()
operator|.
name|setState
argument_list|(
name|this
operator|.
name|state
operator|.
name|convert
argument_list|()
argument_list|)
operator|.
name|setTable
argument_list|(
name|ProtobufUtil
operator|.
name|toProtoTableName
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
argument_list|)
operator|.
name|setTimestamp
argument_list|(
name|this
operator|.
name|timestamp
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * Covert from PB version of TableState    * @param tableState convert from    * @return POJO    */
specifier|public
specifier|static
name|TableState
name|convert
parameter_list|(
name|HBaseProtos
operator|.
name|TableState
name|tableState
parameter_list|)
block|{
name|TableState
operator|.
name|State
name|state
init|=
name|State
operator|.
name|convert
argument_list|(
name|tableState
operator|.
name|getState
argument_list|()
argument_list|)
decl_stmt|;
return|return
operator|new
name|TableState
argument_list|(
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|tableState
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|state
argument_list|,
name|tableState
operator|.
name|getTimestamp
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Static version of state checker    * @param state desired    * @param target equals to any of    * @return true if satisfies    */
specifier|public
specifier|static
name|boolean
name|isInStates
parameter_list|(
name|State
name|state
parameter_list|,
name|State
modifier|...
name|target
parameter_list|)
block|{
for|for
control|(
name|State
name|tableState
range|:
name|target
control|)
block|{
if|if
condition|(
name|state
operator|.
name|equals
argument_list|(
name|tableState
argument_list|)
condition|)
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

