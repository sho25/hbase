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
operator|.
name|codec
operator|.
name|prefixtree
operator|.
name|encode
operator|.
name|tokenize
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

begin_comment
comment|/**  * Warning: currently unused, but code is valid.  Pending performance testing on more data sets.  *  * Where is the key relative to our current position in the tree. For example, the current tree node  * is "BEFORE" the key we are seeking  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
enum|enum
name|TokenizerRowSearchPosition
block|{
name|AFTER
block|,
comment|//the key is after this tree node, so keep searching
name|BEFORE
block|,
comment|//in a binary search, this tells us to back up
name|MATCH
block|,
comment|//the current node is a full match
name|NO_MATCH
block|,
comment|//might as well return a value more informative than null
block|}
end_enum

end_unit

