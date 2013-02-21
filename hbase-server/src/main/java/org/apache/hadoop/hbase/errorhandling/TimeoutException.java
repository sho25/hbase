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
name|errorhandling
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

begin_comment
comment|/**  * Exception for timeout of a task.  * @see TimeoutExceptionInjector  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
specifier|public
class|class
name|TimeoutException
extends|extends
name|Exception
block|{
specifier|private
specifier|final
name|String
name|sourceName
decl_stmt|;
specifier|private
specifier|final
name|long
name|start
decl_stmt|;
specifier|private
specifier|final
name|long
name|end
decl_stmt|;
specifier|private
specifier|final
name|long
name|expected
decl_stmt|;
comment|/**    * Exception indicating that an operation attempt has timed out    * @param start time the operation started (ms since epoch)    * @param end time the timeout was triggered (ms since epoch)    * @param expected expected amount of time for the operation to complete (ms) (ideally, expected<= end-start)    */
specifier|public
name|TimeoutException
parameter_list|(
name|String
name|sourceName
parameter_list|,
name|long
name|start
parameter_list|,
name|long
name|end
parameter_list|,
name|long
name|expected
parameter_list|)
block|{
name|super
argument_list|(
literal|"Timeout elapsed! Source:"
operator|+
name|sourceName
operator|+
literal|" Start:"
operator|+
name|start
operator|+
literal|", End:"
operator|+
name|end
operator|+
literal|", diff:"
operator|+
operator|(
name|end
operator|-
name|start
operator|)
operator|+
literal|", max:"
operator|+
name|expected
operator|+
literal|" ms"
argument_list|)
expr_stmt|;
name|this
operator|.
name|sourceName
operator|=
name|sourceName
expr_stmt|;
name|this
operator|.
name|start
operator|=
name|start
expr_stmt|;
name|this
operator|.
name|end
operator|=
name|end
expr_stmt|;
name|this
operator|.
name|expected
operator|=
name|expected
expr_stmt|;
block|}
specifier|public
name|long
name|getStart
parameter_list|()
block|{
return|return
name|start
return|;
block|}
specifier|public
name|long
name|getEnd
parameter_list|()
block|{
return|return
name|end
return|;
block|}
specifier|public
name|long
name|getMaxAllowedOperationTime
parameter_list|()
block|{
return|return
name|expected
return|;
block|}
specifier|public
name|String
name|getSourceName
parameter_list|()
block|{
return|return
name|sourceName
return|;
block|}
block|}
end_class

end_unit

