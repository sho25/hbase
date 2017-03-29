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
name|procedure2
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Basic ProcedureEvent that contains an "object", which can be a description or a reference to the  * resource to wait on, and a queue for suspended procedures.  * Access to suspended procedures queue is 'synchronized' on the event itself.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ProcedureEvent
parameter_list|<
name|T
parameter_list|>
block|{
specifier|private
specifier|final
name|T
name|object
decl_stmt|;
specifier|private
name|boolean
name|ready
init|=
literal|false
decl_stmt|;
specifier|private
name|ProcedureDeque
name|suspendedProcedures
init|=
operator|new
name|ProcedureDeque
argument_list|()
decl_stmt|;
specifier|public
name|ProcedureEvent
parameter_list|(
specifier|final
name|T
name|object
parameter_list|)
block|{
name|this
operator|.
name|object
operator|=
name|object
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isReady
parameter_list|()
block|{
return|return
name|ready
return|;
block|}
specifier|synchronized
name|void
name|setReady
parameter_list|(
specifier|final
name|boolean
name|isReady
parameter_list|)
block|{
name|this
operator|.
name|ready
operator|=
name|isReady
expr_stmt|;
block|}
specifier|public
name|ProcedureDeque
name|getSuspendedProcedures
parameter_list|()
block|{
return|return
name|suspendedProcedures
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" for "
operator|+
name|object
operator|+
literal|", ready="
operator|+
name|isReady
argument_list|()
operator|+
literal|", suspended procedures count="
operator|+
name|getSuspendedProcedures
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
block|}
end_class

end_unit

