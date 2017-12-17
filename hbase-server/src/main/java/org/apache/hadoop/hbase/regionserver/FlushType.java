begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Reasons we flush.  * @see MemStoreFlusher  * @see FlushRequester  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Private
enum|enum
name|FlushType
block|{
name|NORMAL
block|,
name|ABOVE_ONHEAP_LOWER_MARK
block|,
comment|/* happens due to lower mark breach of onheap memstore settings                               An offheap memstore can even breach the onheap_lower_mark*/
name|ABOVE_ONHEAP_HIGHER_MARK
block|,
comment|/* happens due to higher mark breach of onheap memstore settings                               An offheap memstore can even breach the onheap_higher_mark*/
name|ABOVE_OFFHEAP_LOWER_MARK
block|,
comment|/* happens due to lower mark breach of offheap memstore settings*/
name|ABOVE_OFFHEAP_HIGHER_MARK
comment|/* happens due to higer mark breach of offheap memstore settings*/
block|}
end_enum

end_unit

