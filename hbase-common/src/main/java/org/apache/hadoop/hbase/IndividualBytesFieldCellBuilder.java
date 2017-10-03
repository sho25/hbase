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
package|;
end_package

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

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|IndividualBytesFieldCellBuilder
extends|extends
name|ExtendedCellBuilderImpl
block|{
annotation|@
name|Override
specifier|public
name|ExtendedCell
name|innerBuild
parameter_list|()
block|{
return|return
operator|new
name|IndividualBytesFieldCell
argument_list|(
name|row
argument_list|,
name|rOffset
argument_list|,
name|rLength
argument_list|,
name|family
argument_list|,
name|fOffset
argument_list|,
name|fLength
argument_list|,
name|qualifier
argument_list|,
name|qOffset
argument_list|,
name|qLength
argument_list|,
name|timestamp
argument_list|,
name|type
argument_list|,
name|seqId
argument_list|,
name|value
argument_list|,
name|vOffset
argument_list|,
name|vLength
argument_list|,
name|tags
argument_list|,
name|tagsOffset
argument_list|,
name|tagsLength
argument_list|)
return|;
block|}
block|}
end_class

end_unit

