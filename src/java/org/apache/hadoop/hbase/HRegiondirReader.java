begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
import|;
end_import

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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|hadoop
operator|.
name|fs
operator|.
name|PathFilter
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
name|io
operator|.
name|Text
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
name|util
operator|.
name|Writables
import|;
end_import

begin_comment
comment|/**  * A standalone HRegion directory reader.  Currently reads content on  * file system only.  * TODO: Add dumping of HStoreFile content and HLog.  */
end_comment

begin_class
class|class
name|HRegiondirReader
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|Path
name|parentdir
decl_stmt|;
specifier|static
specifier|final
name|Pattern
name|REGION_NAME_PARSER
init|=
name|Pattern
operator|.
name|compile
argument_list|(
name|HConstants
operator|.
name|HREGIONDIR_PREFIX
operator|+
literal|"([^_]+)_([^_]*)_([^_]*)"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|USAGE
init|=
literal|"Usage: "
operator|+
literal|"java org.apache.hadoop.hbase.HRegionDirReader<regiondir> "
operator|+
literal|"[<tablename>]"
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|infos
decl_stmt|;
name|HRegiondirReader
parameter_list|(
specifier|final
name|HBaseConfiguration
name|conf
parameter_list|,
specifier|final
name|String
name|parentdirName
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|this
operator|.
name|parentdir
operator|=
operator|new
name|Path
argument_list|(
name|parentdirName
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|fs
operator|.
name|exists
argument_list|(
name|parentdir
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|FileNotFoundException
argument_list|(
name|parentdirName
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|fs
operator|.
name|getFileStatus
argument_list|(
name|parentdir
argument_list|)
operator|.
name|isDir
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|parentdirName
operator|+
literal|" not a directory"
argument_list|)
throw|;
block|}
comment|// Look for regions in parentdir.
name|Path
index|[]
name|regiondirs
init|=
name|fs
operator|.
name|listPaths
argument_list|(
name|parentdir
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
comment|/* (non-Javadoc)          * @see org.apache.hadoop.fs.PathFilter#accept(org.apache.hadoop.fs.Path)          */
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
name|Matcher
name|m
init|=
name|REGION_NAME_PARSER
operator|.
name|matcher
argument_list|(
name|path
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|m
operator|!=
literal|null
operator|&&
name|m
operator|.
name|matches
argument_list|()
return|;
block|}
block|}
argument_list|)
decl_stmt|;
comment|// Create list of HRegionInfos for all regions found in
comment|// parentdir.
name|this
operator|.
name|infos
operator|=
operator|new
name|ArrayList
argument_list|<
name|HRegionInfo
argument_list|>
argument_list|()
expr_stmt|;
for|for
control|(
name|Path
name|d
range|:
name|regiondirs
control|)
block|{
name|Matcher
name|m
init|=
name|REGION_NAME_PARSER
operator|.
name|matcher
argument_list|(
name|d
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|m
operator|==
literal|null
operator|||
operator|!
name|m
operator|.
name|matches
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unparseable region dir name"
argument_list|)
throw|;
block|}
name|String
name|tableName
init|=
name|m
operator|.
name|group
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|endKey
init|=
name|m
operator|.
name|group
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|long
name|regionid
init|=
name|Long
operator|.
name|parseLong
argument_list|(
name|m
operator|.
name|group
argument_list|(
literal|3
argument_list|)
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|desc
init|=
name|getTableDescriptor
argument_list|(
name|fs
argument_list|,
name|d
argument_list|,
name|tableName
argument_list|)
decl_stmt|;
name|HRegionInfo
name|info
init|=
operator|new
name|HRegionInfo
argument_list|(
name|regionid
argument_list|,
name|desc
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
operator|(
name|endKey
operator|==
literal|null
operator|||
name|endKey
operator|.
name|length
argument_list|()
operator|==
literal|0
operator|)
condition|?
operator|new
name|Text
argument_list|()
else|:
operator|new
name|Text
argument_list|(
name|endKey
argument_list|)
argument_list|)
decl_stmt|;
name|infos
operator|.
name|add
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Returns a populated table descriptor.    * @param fs Current filesystem.    * @param d The regiondir for<code>tableName</code>    * @param tableName Name of this table.    * @return A HTableDescriptor populated with all known column    * families.    * @throws IOException    */
specifier|private
name|HTableDescriptor
name|getTableDescriptor
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|d
parameter_list|,
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|Text
index|[]
name|families
init|=
name|getFamilies
argument_list|(
name|fs
argument_list|,
name|d
argument_list|)
decl_stmt|;
for|for
control|(
name|Text
name|f
range|:
name|families
control|)
block|{
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|f
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|desc
return|;
block|}
comment|/**    * Get column families for this region by looking at    * directory names under this region.    * This is a hack. HRegions only know what columns they have    * because they are told by passed-in metadata.    * @param regiondir    * @return Array of family names.    * @throws IOException    */
specifier|private
name|Text
index|[]
name|getFamilies
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|regiondir
parameter_list|)
throws|throws
name|IOException
block|{
name|Path
index|[]
name|subdirs
init|=
name|fs
operator|.
name|listPaths
argument_list|(
name|regiondir
argument_list|,
operator|new
name|PathFilter
argument_list|()
block|{
specifier|public
name|boolean
name|accept
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
operator|!
name|path
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
literal|"log"
argument_list|)
return|;
block|}
block|}
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Text
argument_list|>
name|families
init|=
operator|new
name|ArrayList
argument_list|<
name|Text
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Path
name|d
range|:
name|subdirs
control|)
block|{
comment|// Convert names of subdirectories into column family names
comment|// by adding the colon.
name|Text
name|family
init|=
operator|new
name|Text
argument_list|(
name|d
operator|.
name|getName
argument_list|()
operator|+
literal|":"
argument_list|)
decl_stmt|;
name|families
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
return|return
name|families
operator|.
name|toArray
argument_list|(
operator|new
name|Text
index|[]
block|{}
argument_list|)
return|;
block|}
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|getRegions
parameter_list|()
block|{
return|return
name|this
operator|.
name|infos
return|;
block|}
name|HRegionInfo
name|getRegionInfo
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
block|{
name|HRegionInfo
name|result
init|=
literal|null
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|i
range|:
name|getRegions
argument_list|()
control|)
block|{
if|if
condition|(
name|i
operator|.
name|tableDesc
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|result
operator|=
name|i
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"No such table: "
operator|+
name|tableName
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
specifier|private
name|void
name|dump
parameter_list|(
specifier|final
name|String
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|dump
argument_list|(
name|getRegionInfo
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|dump
parameter_list|(
specifier|final
name|HRegionInfo
name|info
parameter_list|)
throws|throws
name|IOException
block|{
name|HRegion
name|r
init|=
operator|new
name|HRegion
argument_list|(
name|this
operator|.
name|parentdir
argument_list|,
literal|null
argument_list|,
name|FileSystem
operator|.
name|get
argument_list|(
name|this
operator|.
name|conf
argument_list|)
argument_list|,
name|conf
argument_list|,
name|info
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|Text
index|[]
name|families
init|=
name|info
operator|.
name|tableDesc
operator|.
name|families
argument_list|()
operator|.
name|keySet
argument_list|()
operator|.
name|toArray
argument_list|(
operator|new
name|Text
index|[]
block|{}
argument_list|)
decl_stmt|;
name|HInternalScannerInterface
name|scanner
init|=
name|r
operator|.
name|getScanner
argument_list|(
name|families
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HStoreKey
name|key
init|=
operator|new
name|HStoreKey
argument_list|()
decl_stmt|;
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|results
init|=
operator|new
name|TreeMap
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
comment|// Print out table header line.
name|String
name|s
init|=
name|info
operator|.
name|startKey
operator|.
name|toString
argument_list|()
decl_stmt|;
name|String
name|startKey
init|=
operator|(
name|s
operator|==
literal|null
operator|||
name|s
operator|.
name|length
argument_list|()
operator|<=
literal|0
operator|)
condition|?
literal|"<>"
else|:
name|s
decl_stmt|;
name|s
operator|=
name|info
operator|.
name|endKey
operator|.
name|toString
argument_list|()
expr_stmt|;
name|String
name|endKey
init|=
operator|(
name|s
operator|==
literal|null
operator|||
name|s
operator|.
name|length
argument_list|()
operator|<=
literal|0
operator|)
condition|?
literal|"<>"
else|:
name|s
decl_stmt|;
name|String
name|tableName
init|=
name|info
operator|.
name|tableDesc
operator|.
name|getName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"table: "
operator|+
name|tableName
operator|+
literal|", regionid: "
operator|+
name|info
operator|.
name|regionId
operator|+
literal|", startkey: "
operator|+
name|startKey
operator|+
literal|", endkey: "
operator|+
name|endKey
argument_list|)
expr_stmt|;
comment|// Now print rows.  Offset by a space to distingush rows from
comment|// table headers. TODO: Add in better formatting of output.
comment|// Every line starts with row name followed by column name
comment|// followed by cell content.
while|while
condition|(
name|scanner
operator|.
name|next
argument_list|(
name|key
argument_list|,
name|results
argument_list|)
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Text
argument_list|,
name|byte
index|[]
argument_list|>
name|es
range|:
name|results
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|Text
name|colname
init|=
name|es
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|byte
index|[]
name|colvalue
init|=
name|es
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Object
name|value
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|colname
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
literal|"info:regioninfo"
argument_list|)
condition|)
block|{
name|value
operator|=
name|Writables
operator|.
name|getWritable
argument_list|(
name|colvalue
argument_list|,
operator|new
name|HRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
operator|new
name|String
argument_list|(
name|colvalue
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|" "
operator|+
name|key
operator|+
literal|", "
operator|+
name|colname
operator|.
name|toString
argument_list|()
operator|+
literal|": \""
operator|+
name|value
operator|.
name|toString
argument_list|()
operator|+
literal|"\""
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * @param args    * @throws IOException    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|1
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
name|USAGE
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
name|HBaseConfiguration
name|c
init|=
operator|new
name|HBaseConfiguration
argument_list|()
decl_stmt|;
name|HRegiondirReader
name|reader
init|=
operator|new
name|HRegiondirReader
argument_list|(
name|c
argument_list|,
name|args
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|args
operator|.
name|length
operator|==
literal|1
condition|)
block|{
comment|// Do all regions.
for|for
control|(
name|HRegionInfo
name|info
range|:
name|reader
operator|.
name|getRegions
argument_list|()
control|)
block|{
name|reader
operator|.
name|dump
argument_list|(
name|info
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|reader
operator|.
name|dump
argument_list|(
name|args
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

