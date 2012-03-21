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
name|coprocessor
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|RowProcessor
import|;
end_import

begin_comment
comment|/**  * This class demonstrates how to implement atomic read-modify-writes  * using {@link HRegion#processRowsWithLocks()} and Coprocessor endpoints.  */
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
specifier|public
specifier|abstract
class|class
name|BaseRowProcessorEndpoint
extends|extends
name|BaseEndpointCoprocessor
implements|implements
name|RowProcessorProtocol
block|{
comment|/**    * Pass a processor to HRegion to process multiple rows atomically.    *     * The RowProcessor implementations should be the inner classes of your    * RowProcessorEndpoint. This way the RowProcessor can be class-loaded with    * the Coprocessor endpoint together.    *    * See {@link TestRowProcessorEndpoint} for example.    *    * @param processor The object defines the read-modify-write procedure    * @return The processing result    */
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|process
parameter_list|(
name|RowProcessor
argument_list|<
name|T
argument_list|>
name|processor
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|region
init|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
decl_stmt|;
name|region
operator|.
name|processRowsWithLocks
argument_list|(
name|processor
argument_list|)
expr_stmt|;
return|return
name|processor
operator|.
name|getResult
argument_list|()
return|;
block|}
block|}
end_class

end_unit

