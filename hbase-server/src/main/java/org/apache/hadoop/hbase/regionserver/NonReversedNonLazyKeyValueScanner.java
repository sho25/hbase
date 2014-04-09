begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|commons
operator|.
name|lang
operator|.
name|NotImplementedException
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
name|hbase
operator|.
name|Cell
import|;
end_import

begin_comment
comment|/**  * A "non-reversed& non-lazy" scanner which does not support backward scanning  * and always does a real seek operation. Most scanners are inherited from this  * class.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|NonReversedNonLazyKeyValueScanner
extends|extends
name|NonLazyKeyValueScanner
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|backwardSeek
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"backwardSeek must not be called on a "
operator|+
literal|"non-reversed scanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seekToPreviousRow
parameter_list|(
name|Cell
name|key
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"seekToPreviousRow must not be called on a "
operator|+
literal|"non-reversed scanner"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|seekToLastRow
parameter_list|()
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"seekToLastRow must not be called on a "
operator|+
literal|"non-reversed scanner"
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

