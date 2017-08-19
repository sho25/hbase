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
name|regionserver
operator|.
name|compactions
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|fs
operator|.
name|FileSystem
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
name|fs
operator|.
name|Path
import|;
end_import

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
name|AbstractMultiFileWriter
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
name|AbstractMultiFileWriter
operator|.
name|WriterFactory
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
name|InternalScanner
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
name|Store
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
name|StoreFileWriter
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
name|StoreScanner
import|;
end_import

begin_comment
comment|/**  * Base class for implementing a Compactor which will generate multiple output files after  * compaction.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|AbstractMultiOutputCompactor
parameter_list|<
name|T
extends|extends
name|AbstractMultiFileWriter
parameter_list|>
extends|extends
name|Compactor
argument_list|<
name|T
argument_list|>
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AbstractMultiOutputCompactor
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|AbstractMultiOutputCompactor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|Store
name|store
parameter_list|)
block|{
name|super
argument_list|(
name|conf
argument_list|,
name|store
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|initMultiWriter
parameter_list|(
name|AbstractMultiFileWriter
name|writer
parameter_list|,
name|InternalScanner
name|scanner
parameter_list|,
specifier|final
name|FileDetails
name|fd
parameter_list|,
specifier|final
name|boolean
name|shouldDropBehind
parameter_list|)
block|{
name|WriterFactory
name|writerFactory
init|=
operator|new
name|WriterFactory
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StoreFileWriter
name|createWriter
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|createTmpWriter
argument_list|(
name|fd
argument_list|,
name|shouldDropBehind
argument_list|)
return|;
block|}
block|}
decl_stmt|;
comment|// Prepare multi-writer, and perform the compaction using scanner and writer.
comment|// It is ok here if storeScanner is null.
name|StoreScanner
name|storeScanner
init|=
operator|(
name|scanner
operator|instanceof
name|StoreScanner
operator|)
condition|?
operator|(
name|StoreScanner
operator|)
name|scanner
else|:
literal|null
decl_stmt|;
name|writer
operator|.
name|init
argument_list|(
name|storeScanner
argument_list|,
name|writerFactory
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|abortWriter
parameter_list|(
name|T
name|writer
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|store
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|leftoverFile
range|:
name|writer
operator|.
name|abortWriters
argument_list|()
control|)
block|{
try|try
block|{
name|fs
operator|.
name|delete
argument_list|(
name|leftoverFile
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Failed to delete the leftover file "
operator|+
name|leftoverFile
operator|+
literal|" after an unfinished compaction."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

