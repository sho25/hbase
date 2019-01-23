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
name|util
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
comment|/**  * Utility methods for dealing with Collections, including treating null collections as empty.  * @deprecated Since 2.0.6/2.1.3/2.2.0  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
comment|//HBASE-20215: Marking as deprecated. Although audience is private, there might be references to
comment|//this class on some client classpath.
annotation|@
name|Deprecated
specifier|public
class|class
name|CollectionUtils
extends|extends
name|ConcurrentMapUtils
block|{  }
end_class

end_unit

