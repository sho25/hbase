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
name|security
operator|.
name|visibility
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

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|VisibilityLabelOrdinalProvider
block|{
comment|/**    * @param label Not null label string    * @return The ordinal for the label. The ordinal starts from 1. Returns 0 when passed a non    *         existing label.    */
specifier|public
name|int
name|getLabelOrdinal
parameter_list|(
name|String
name|label
parameter_list|)
function_decl|;
comment|/**    * Returns the string associated with the ordinal. Not be used in MR.    * @param ordinal representing the visibility label's ordinal    * @return label associated with the string, null if not found    */
specifier|public
name|String
name|getLabel
parameter_list|(
name|int
name|ordinal
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

