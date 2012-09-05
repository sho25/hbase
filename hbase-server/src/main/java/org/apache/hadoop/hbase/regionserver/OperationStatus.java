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
name|regionserver
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
name|HConstants
operator|.
name|OperationStatusCode
import|;
end_import

begin_comment
comment|/**  *   * This class stores the Operation status code and the exception message  * that occurs in case of failure of operations like put, delete, etc.  * This class is added with a purpose of adding more details or info regarding  * the operation status in future.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|OperationStatus
block|{
comment|/** Singleton for successful operations.  */
specifier|static
specifier|final
name|OperationStatus
name|SUCCESS
init|=
operator|new
name|OperationStatus
argument_list|(
name|OperationStatusCode
operator|.
name|SUCCESS
argument_list|)
decl_stmt|;
comment|/** Singleton for failed operations.  */
specifier|static
specifier|final
name|OperationStatus
name|FAILURE
init|=
operator|new
name|OperationStatus
argument_list|(
name|OperationStatusCode
operator|.
name|FAILURE
argument_list|)
decl_stmt|;
comment|/** Singleton for operations not yet run.  */
specifier|static
specifier|final
name|OperationStatus
name|NOT_RUN
init|=
operator|new
name|OperationStatus
argument_list|(
name|OperationStatusCode
operator|.
name|NOT_RUN
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|OperationStatusCode
name|code
decl_stmt|;
specifier|private
specifier|final
name|String
name|exceptionMsg
decl_stmt|;
specifier|public
name|OperationStatus
parameter_list|(
name|OperationStatusCode
name|code
parameter_list|)
block|{
name|this
argument_list|(
name|code
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
specifier|public
name|OperationStatus
parameter_list|(
name|OperationStatusCode
name|code
parameter_list|,
name|String
name|exceptionMsg
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|exceptionMsg
operator|=
name|exceptionMsg
expr_stmt|;
block|}
comment|/**    * @return OperationStatusCode    */
specifier|public
name|OperationStatusCode
name|getOperationStatusCode
parameter_list|()
block|{
return|return
name|code
return|;
block|}
comment|/**    * @return ExceptionMessge    */
specifier|public
name|String
name|getExceptionMsg
parameter_list|()
block|{
return|return
name|exceptionMsg
return|;
block|}
block|}
end_class

end_unit

